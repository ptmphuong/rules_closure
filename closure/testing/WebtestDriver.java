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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.Duration;
import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class WebtestDriver {

  private static final Logger logger = Logger.getLogger(WebtestDriver.class.getName());

  private WebDriver driver;
  private String runURL;

  public WebtestDriver(String runURL) {
    this.driver = new WebTest().newWebDriverSession();
    this.runURL = runURL;
  }

  public void run() {
    int port = 8080;

    driver.manage().timeouts().setScriptTimeout(60, SECONDS);
    logger.info("RunURL is: " + this.runURL);
    driver.get(this.runURL);

    new FluentWait<>((JavascriptExecutor) driver)
        .pollingEvery(Duration.ofMillis(100))
        .withTimeout(Duration.ofSeconds(5))
        .until(executor -> {
          boolean finishedSuccessfully = (boolean) executor.executeScript("return window.top.G_testRunner.isFinished()");
          if (!finishedSuccessfully) {
            logger.log(Level.SEVERE, "G_testRunner has not finished successfully");
          }
          return true;
        }
    );

    String testReport = ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.getReport();").toString();
    logger.info(testReport);

    boolean allTestsPassed = (boolean) ((JavascriptExecutor) driver).executeScript("return window.top.G_testRunner.isSuccess();");

    driver.quit();

    if (!allTestsPassed) {
      System.exit(1);
    }
  }
}
