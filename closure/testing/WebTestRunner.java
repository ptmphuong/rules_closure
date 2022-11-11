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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.io.IOException;

import rules_closure.closure.testing.FileServerHandler;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.net.ServerSocketFactory;
import com.google.common.collect.ImmutableList;

import com.google.common.net.HostAndPort;
import io.bazel.rules.closure.webfiles.BuildInfo.Webfiles;
import io.bazel.rules.closure.webfiles.BuildInfo.WebfilesSource;
import io.bazel.rules.closure.webfiles.server.BuildInfo.AssetInfo;
import io.bazel.rules.closure.webfiles.server.BuildInfo.WebfilesServerInfo;
import io.bazel.rules.closure.webfiles.server.WebfilesServer;
import io.bazel.rules.closure.webfiles.server.DaggerWebfilesServer_Server;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.nio.charset.StandardCharsets.UTF_8;

class WebTestRunner {
  public static void main(String args[]) throws Exception {
    String testURL = args[1];
    if (!testURL.startsWith("/")) {
      testURL = "/" + testURL;
    }
    log("testURL is: " + testURL);

    int webFilePort = 6006;
    int customPort = PortProber.findFreePort();
    int port = customPort;
    // int port = webFilePort;

    // START CUSTOM SERVER
    String currentDir = System.getProperty("user.dir");
    String fullDir = currentDir + testURL;
    log("currentDir is: " + currentDir);
    log ("fulldir is: " + fullDir);
    HttpServer server = null;
    if (port == customPort) {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        HttpContext context = server.createContext("/", new FileServerHandler(currentDir));
        server.start();
    }

    // START WEBFILE SERVER
    WebfilesServerInfo CONFIG =
        WebfilesServerInfo.newBuilder()
            .setLabel("Webtest")
            .setBind("localhost:" + port)
            .addManifest("/manifest.pbtxt")
            .build();

    Webfiles MANIFEST =
        Webfiles.newBuilder()
            .addSrc(
                WebfilesSource.newBuilder()
                    .setWebpath(testURL)
                    .setPath(currentDir)
                    .setLongpath(testURL)
                    // .setPath("webfile.html")
                    // .setLongpath("/webfile.html")
                    .build())
            .build();

    FileSystem fs = Jimfs.newFileSystem(Configuration.forCurrentPlatform());
    Files.write(fs.getPath("/manifest.pbtxt"), MANIFEST.toString().getBytes(UTF_8));
    Files.write(fs.getPath("/config.pbtxt"), CONFIG.toString().getBytes(UTF_8));
    Files.write(fs.getPath("/webfile.html"), "anybody in there?".getBytes(UTF_8));

    ExecutorService serverExecutor = Executors.newCachedThreadPool();
    WebfilesServer wfserver =
        DaggerWebfilesServer_Server.builder()
            .args(ImmutableList.of("/config.pbtxt"))
            .executor(serverExecutor)
            .fs(fs)
            .serverSocketFactory(ServerSocketFactory.getDefault())
            .build()
            .server();

    if (port == webFilePort) {
        HostAndPort address = wfserver.spawn();
        log("webfile server running at: " + address.toString());
    }

    // START WEBDRIVER
    WebDriver driver = new WebTest().newWebDriverSession();
    driver.manage().timeouts().setScriptTimeout(60, SECONDS);
    String runURL = "http://localhost:" + port + testURL;
    log("RunURL is: " + runURL);
    driver.get(runURL);

    if (driver.getPageSource().contains("500")) {
      log("cannot find file");
      System.exit(1);
    }

    // WAIT FOR TESTS TO FINISH
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

    // LOG TEST REPORT
    String testReport = ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.getReport();").toString();
    log(testReport);

    boolean allTestsPassed = (boolean) ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.isSuccess();");

    // CLEAN UP
    driver.quit();
    if (port == customPort) {
      server.stop(0);
    } else {
      serverExecutor.shutdownNow();
    }

    if (!allTestsPassed) {
      System.exit(1);
    }
  }

  private static void log(String s) {
    Logger.getGlobal().info(s);
  }
}
