package rules_closure.closure.testing;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;

import java.util.logging.Logger;
import java.net.ServerSocket;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import com.google.common.net.HostAndPort;
import io.bazel.rules.closure.webfiles.BuildInfo.Webfiles;
import io.bazel.rules.closure.webfiles.BuildInfo.WebfilesSource;
import io.bazel.rules.closure.webfiles.server.BuildInfo.AssetInfo;
import io.bazel.rules.closure.webfiles.server.BuildInfo.WebfilesServerInfo;
import io.bazel.rules.closure.webfiles.server.WebfilesServer;
import io.bazel.rules.closure.webfiles.server.DaggerWebfilesServer_Server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;
import com.google.common.collect.ImmutableList;

import static java.nio.charset.StandardCharsets.UTF_8;

class ClosureServer {
  public static void main(String args[]) throws Exception {
    // String testURL = args[1];
    String testURL = "/closure/testing/test/gen_arithmetic_module_test.html";
    if (!testURL.startsWith("/")) {
      testURL = "/" + testURL;
    }
  log("testURL is: " + testURL);

    // server doing server stuff

    WebfilesServerInfo CONFIG =
        WebfilesServerInfo.newBuilder()
            .setLabel("//label")
            .setBind("localhost:6006")
            .addManifest("/manifest.pbtxt")
            .addExternalAsset(
                AssetInfo.newBuilder().setWebpath("/external.txt").setPath("/external.txt").build())
            .build();

    Webfiles MANIFEST =
        Webfiles.newBuilder()
            .addSrc(
                WebfilesSource.newBuilder()
                    .setWebpath(testURL)
                    .setPath("/webfile.html")
                    .setLongpath("/webfile.html")
                    .build())
            .build();


    FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

    String htmlTemplate = "<!doctype html><html><head></head><body>"
        + "<script>\\n"
        + "  var CLOSURE_NO_DEPS = true;\\n"
        + "  var CLOSURE_UNCOMPILED_DEFINES = {};\\n"
        + "</script>\\n"
        + "<script src=\"arithmetic_module_test_bin.js\"></script>\\n"
        + "</body></html>";

    Files.write(fs.getPath("/external.txt"), "hello".getBytes(UTF_8));
    Files.write(fs.getPath("/webfile.html"), htmlTemplate.getBytes(UTF_8));
    Files.write(fs.getPath("/manifest.pbtxt"), MANIFEST.toString().getBytes(UTF_8));
    Files.write(fs.getPath("/config.pbtxt"), CONFIG.toString().getBytes(UTF_8));

    ExecutorService executor = Executors.newCachedThreadPool();
    WebfilesServer server =
        DaggerWebfilesServer_Server.builder()
            .args(ImmutableList.of("/config.pbtxt"))
            .executor(executor)
            .fs(fs)
            .serverSocketFactory(ServerSocketFactory.getDefault())
            .build()
            .server();

    HostAndPort address = server.spawn();
    log("running at: " + address.toString());
  }

  private static void log(String s) {
    Logger.getGlobal().info(s);
  }
}
// TODO: how to pass the html file in?
//
