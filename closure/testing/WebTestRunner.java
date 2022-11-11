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

import rules_closure.closure.testing.FileServerHandler;

import static java.util.concurrent.TimeUnit.SECONDS;

class WebTestRunner {
  public static void main(String args[]) throws IOException {
    // TODO: What's a good way to parse args? Ex: --test-url /srcs/gen_HelloWorldTest.html
    String testURL = args[1];
    if (!testURL.startsWith("/")) {
      testURL = "/" + testURL;
    }
    log("testURL is: " + testURL);

    // START CUSTOM SERVER
    // int port = 6006;
    int port = PortProber.findFreePort();
    String currentDir = System.getProperty("user.dir");
    log("currentDir is: " + currentDir);
    log ("fulldir is: " + currentDir + testURL);
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    HttpContext context = server.createContext("/", new FileServerHandler(currentDir));
    server.start();

    String runURL = "http://localhost:" + port + testURL;
    log("RunURL is: " + runURL);

    // START WEBDRIVER
    WebDriver driver = new WebTest().newWebDriverSession();
    driver.manage().timeouts().setScriptTimeout(60, SECONDS);
    driver.get(runURL);

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
    server.stop(0);

    if (!allTestsPassed) {
      System.exit(1);
    }

    // fail on purpose to check log
    // System.exit(1);
  }

  private static void log(String s) {
    Logger.getGlobal().info(s);
  }
}
