package rules_closure.closure.testing;

/*
 *  This program starts an HTTP server that serves runfiles.
 *  It uses a webdriver to load the generated test runner HTML file
 *  on the browser. Once the page is loaded, it polls the Closure
 *  Library repeatedly to check if the tests are finished, and logs results.
 */

import com.google.testing.web.WebTest;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.FluentWait;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;

import java.util.logging.Logger;
import java.net.ServerSocket;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import rules_closure.closure.testing.FileServerHandler;

import com.google.common.net.HostAndPort;
import io.bazel.rules.closure.webfiles.BuildInfo.Webfiles;
import io.bazel.rules.closure.webfiles.BuildInfo.WebfilesSource;
import io.bazel.rules.closure.webfiles.server.BuildInfo.AssetInfo;
import io.bazel.rules.closure.webfiles.server.BuildInfo.WebfilesServerInfo;
import io.bazel.rules.closure.webfiles.server.WebfilesServer;
import io.bazel.rules.closure.webfiles.server.DaggerWebfilesServer_Server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;
import com.google.common.collect.ImmutableList;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;

class WebTestRunner {
  // public static void main(String args[]) throws IOException {
  public static void main(String args[]) throws Exception {
    // TODO: What's a good way to parse args? Ex: --test-url /srcs/gen_HelloWorldTest.html

    String testURL = args[1];
    // String testURL = "/closure/testing/test/gen_arithmetic_module_test.html";
    if (!testURL.startsWith("/")) {
      testURL = "/" + testURL;
    }
    log("testURL is: " + testURL);

    int port = PortProber.findFreePort();
    // int port = 6006;

    // START CUSTOM SERVER
    String currentDir = System.getProperty("user.dir");

    HttpServer customServer = HttpServer.create(new InetSocketAddress(6006), 0);
    HttpContext context = customServer.createContext("/", new FileServerHandler(currentDir));
    customServer.start();

    // ExecutorService serverExecutor = startServer(testURL);

    WebDriver driver = new WebTest().newWebDriverSession();
    driver.manage().timeouts().setScriptTimeout(20, SECONDS);

    String runURL = "http://localhost:" + port + testURL;
    log("RunURL is: " + runURL);

    driver.get(runURL);

    new FluentWait<>((JavascriptExecutor) driver)
        .pollingEvery(Duration.ofMillis(100))
        .withTimeout(Duration.ofSeconds(5))
        .until(executor -> {
          boolean finishedSuccessfully = (boolean) executor.executeScript("return window.top.G_testRunner.isFinished()");
          if (!finishedSuccessfully) {
            log("G_testRunner has not finished successfully");
            System.exit(1);
          }
          return true;
        }
        );

    String testReport = ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.getReport();").toString();
    log(testReport);

    boolean allTestsPassed = (boolean) ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.isSuccess();");

    driver.quit();

    if (!allTestsPassed) {
      System.exit(1);
    }

    customServer.stop(0);
  }

  private static void log(String s) {
    Logger.getGlobal().info(s);
  }

  private static ExecutorService startServer(String testURL) throws Exception {
    WebfilesServerInfo CONFIG =
        WebfilesServerInfo.newBuilder()
            .setLabel("Webtest")
            .setBind("localhost:6006")
            .addManifest("/manifest.pbtxt")
            .build();

    Webfiles MANIFEST =
        Webfiles.newBuilder()
            .addSrc(
                WebfilesSource.newBuilder()
                    .setPath(testURL)
                    .setLongpath(testURL)
                    .setWebpath(testURL)
                    .build())
            .build();

    FileSystem fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform());
    Files.write(fs.getPath("/manifest.pbtxt"), MANIFEST.toString().getBytes(UTF_8));
    Files.write(fs.getPath("/config.pbtxt"), CONFIG.toString().getBytes(UTF_8));

    ExecutorService serverExecutor = Executors.newCachedThreadPool();
    WebfilesServer server =
    DaggerWebfilesServer_Server.builder()
        .args(ImmutableList.of("/config.pbtxt"))
        .executor(serverExecutor)
        .fs(fs)
        .serverSocketFactory(ServerSocketFactory.getDefault())
        .build()
        .server();

    HostAndPort address = server.spawn();
    // String runURL = "http://" + address.toString() + testURL;
    return serverExecutor;
  }

  private static void stopServer(ExecutorService serverExecutor) {
    serverExecutor.shutdownNow();
  }
}

// TODO: how to pass the html file in?
//
