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

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import io.bazel.rules.closure.webfiles.server.WebfilesServer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.net.PortProber;

/**
 * The test runner to run tests against browsers.
 *
 * <p>This program starts an HTTP server that serves runfiles. It uses a webdriver to load the
 * generated test runner HTML file on the browser. Once the page is loaded, it polls the Closure
 * Library repeatedly to check if the tests are finished, and logs results.
 */
class TestRunner {

  private static final Logger logger = Logger.getLogger(TestRunner.class.getName());

  public static void main(String args[]) throws Exception {
    String serverConfig = args[0];
    String htmlWebpath = args[1];
    if (!htmlWebpath.startsWith("/")) {
      htmlWebpath = "/" + htmlWebpath;
    }

    String bind = String.format("bind: \"localhost:%s\"", PortProber.findFreePort());

    WebfilesServer server = null;
    TestDriver driver = null;
    boolean allTestsPassed = false;

    try {
      server = WebfilesServer.create(ImmutableList.of(serverConfig, bind));
      HostAndPort hostAndPort = server.spawn();

      driver = new TestDriver("http://" + hostAndPort + htmlWebpath);
      allTestsPassed = driver.run();
    } finally {
      if (driver != null) {
        driver.quit();
      }
      if (server != null) {
        server.shutdown();
      }
    }

    if (allTestsPassed) {
      logger.info("All tests passed");
      // TODO(#556): Remove this when the server can shutdown properly.
      System.exit(0);
    } else {
      logger.log(
          Level.SEVERE,
          "Test(s) failed.\n"
              + "TIPS: Debug your tests interactively on a browser using 'bazel run"
              + " :<targetname>_debug'");
      System.exit(1);
    }
  }
}

