/*
 * Copyright 2022 The Closure Rules Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rules_closure.closure.testing;

import com.google.testing.web.WebTest;
import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.WebDriver;
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
      logger.log(Level.SEVERE, "Test failed");
    }
  }
}
