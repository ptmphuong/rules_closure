package rules_closure.closure.testing;

/*
 *  This program starts an HTTP server that serves runfiles.
 *  It uses a webdriver to load the generated test runner HTML file
 *  on the browser. Once the page is loaded, it polls the Closure
 *  Library repeatedly to check if the tests are finished, and logs results.
 */

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;

import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.nio.file.FileSystems;
import javax.net.ServerSocketFactory;

import io.bazel.rules.closure.webfiles.server.WebfilesServer;
import io.bazel.rules.closure.webfiles.server.DaggerWebfilesServer_Server;
import rules_closure.closure.testing.MyWebDriver;

class WebTestRunner {
  public static void main(String args[]) throws Exception {

    String configPath = System.getProperty("server_config_path", "no web_config_path");
    String html = System.getProperty("html_web_path", "no html_web_path");
    log("configPath is: " + configPath);
    log("html is: " + html);

    // Start the server
    ExecutorService serverExecutor = Executors.newCachedThreadPool();
    WebfilesServer server =
        DaggerWebfilesServer_Server.builder()
            .args(ImmutableList.of(configPath))
            .executor(serverExecutor)
            .fs(FileSystems.getDefault())
            .serverSocketFactory(ServerSocketFactory.getDefault())
            .build()
            .server();

    HostAndPort hostAndPort = server.spawn();
    String address = hostAndPort.toString();
    log("webfile server running at: " + address);

    // Start the driver
    String runURL = "http://" + address + html;
    log("runURL: " + runURL);
    MyWebDriver driver = new MyWebDriver(runURL);
    driver.run();

    // Clean up
    serverExecutor.shutdownNow();
    System.exit(0);
  }

  private static void log(String s) {
    Logger.getGlobal().info(s);
  }
}
