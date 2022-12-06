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

package io.bazel.rules.closure.testing;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.testing.web.WebTest;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

public class TestDriver {

  private static final Logger logger = Logger.getLogger(TestDriver.class.getName());
  private static final long POLL_INTERVAL = 100;
  private static final long TEST_TIMEOUT = 300;

  private WebDriver driver;
  private String htmlURL;

  public TestDriver(String htmlURL) {
    this.driver = new WebTest().newWebDriverSession();
    this.htmlURL = htmlURL;
  }

  public void run() {
    driver.manage().timeouts().setScriptTimeout(TEST_TIMEOUT, SECONDS);
    logger.info("WebDriver is running on: " + this.htmlURL);
    driver.get(this.htmlURL);

    try {
      new FluentWait<>((JavascriptExecutor) driver)
          .pollingEvery(Duration.ofMillis(POLL_INTERVAL))
          .withTimeout(Duration.ofSeconds(TEST_TIMEOUT))
          .until(
              executor -> {
                boolean finishedSuccessfully =
                    (boolean) executor.executeScript("return window.top.G_testRunner.isFinished()");
                if (!finishedSuccessfully) {
                  failTest("G_testRunner has not finished successfully");
                }
                return true;
              });
    } catch (TimeoutException e) {
      failTest(String.format("Test timeout after %s seconds", TEST_TIMEOUT));
    }

    String testReport =
        ((JavascriptExecutor) driver)
            .executeScript("return window.top.G_testRunner.getReport();")
            .toString();
    logger.info(testReport);

    boolean allTestsPassed =
        (boolean)
            ((JavascriptExecutor) driver)
                .executeScript("return window.top.G_testRunner.isSuccess();");

    driver.quit();

    if (!allTestsPassed) {
      failTest("Test(s) failed.\nTIP: Debug your tests interactively on a browser using 'bazel run :<tagetname>_debug'");
    } else {
      logger.info("All tests passed");
    }
  }

  private static void failTest(String error) {
    logger.log(Level.SEVERE, error);
    System.exit(1);
  }
}

