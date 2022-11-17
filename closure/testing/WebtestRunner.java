package rules_closure.closure.testing;

/*
 *  This program starts an HTTP server that serves runfiles.
 *  It uses a webdriver to load the generated test runner HTML file
 *  on the browser. Once the page is loaded, it polls the Closure
 *  Library repeatedly to check if the tests are finished, and logs results.
 */

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.nio.file.FileSystems;
import javax.net.ServerSocketFactory;

import io.bazel.rules.closure.webfiles.server.WebfilesServer;
import io.bazel.rules.closure.webfiles.server.DaggerWebfilesServer_Server;
import rules_closure.closure.testing.WebtestDriver;

class WebtestRunner {

  public static void main(String args[]) throws Exception {

    String serverConfig = System.getProperty("server_config_path");
    String htmlWebpath = System.getProperty("html_web_path");

    // Start the server
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

    // Start the driver
    String runURL = "http://" + address + htmlWebpath;
    WebtestDriver driver = new WebtestDriver(runURL);
    driver.run();

    // Clean up
    serverExecutor.shutdownNow();
    System.exit(0);
  }
}
