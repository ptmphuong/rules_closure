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

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.concurrent.TimeUnit.SECONDS;

class Driver {
  public static void main(String args[]) throws Exception {
    String testFile = args[0];
    String testURL = args[1];
    String html = args[2];
    html = html + ".html";
    if (!testURL.startsWith("/")) {
      testURL = "/" + testURL;
    }
    if (!html.startsWith("/")) {
      html = "/" + html;
    }
    log("html is: " + html);

    int port = 8080;

    // START WEBDRIVER
    WebDriver driver = new WebTest().newWebDriverSession();
    driver.manage().timeouts().setScriptTimeout(60, SECONDS);
    String runURL = "http://localhost:" + port + html;
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

    if (!allTestsPassed) {
      System.exit(1);
    }

    System.exit(1); // error on purpose to get log
  }

  private static void log(String s) {
    Logger.getGlobal().info(s);
  }
}
