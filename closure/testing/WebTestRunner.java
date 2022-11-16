package rules_closure.closure.testing;

/*
 *  This program starts an HTTP server that serves runfiles.
 *  It uses a webdriver to load the generated test runner HTML file
 *  on the browser. Once the page is loaded, it polls the Closure
 *  Library repeatedly to check if the tests are finished, and logs results.
 */

import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.IOException;
import rules_closure.closure.testing.MyWebDriver;
import java.nio.file.FileSystems;
import com.google.common.collect.ImmutableList;
import javax.net.ServerSocketFactory;

import com.google.common.net.HostAndPort;
import io.bazel.rules.closure.webfiles.server.WebfilesServer;
import io.bazel.rules.closure.webfiles.server.DaggerWebfilesServer_Server;

class WebTestRunner {
  public static void main(String args[]) throws Exception {
    String serverConfig = args[0];
    String html = args[1];
    if (!html.startsWith("/")) {
      html = "/" + html;
    }

    ExecutorService serverExecutor = Executors.newCachedThreadPool();
    WebfilesServer server =
        DaggerWebfilesServer_Server.builder()
            .args(ImmutableList.of(serverConfig))
            .executor(serverExecutor)
            .fs(FileSystems.getDefault())
            .serverSocketFactory(ServerSocketFactory.getDefault())
            .build()
            .server();

    HostAndPort hostAndPort = server.spawn();
    String address = hostAndPort.toString();
    log("webfile server running at: " + address);

    String runURL = "http://" + address + html;
    log("runURL: " + runURL);
    MyWebDriver driver = new MyWebDriver(runURL);
    driver.run();

    serverExecutor.shutdownNow();
    System.exit(0);
  }

  private static void log(String s) {
    Logger.getGlobal().info(s);
  }
}
