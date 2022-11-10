package rules_closure.closure.testing;

/*
 * Custom file server handler used by WebTestRunner.java to serve runfiles.
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLDecoder;

public final class FileServerHandler implements HttpHandler {
  private String rootDir;

  public FileServerHandler(String rootDir) {
    this.rootDir = rootDir;
  }

  public void handle(HttpExchange exchange) throws IOException {
      Path path = Paths.get(
          rootDir,
          URLDecoder.decode(exchange.getRequestURI().toString(), UTF_8)
      ).toAbsolutePath();

      if (path != null) {
          exchange.setAttribute("request-path", path.toString()); 
          if (!Files.exists(path) || !Files.isReadable(path)) {
            handleNotFound(exchange);
          } else {
            handleServeFile(exchange, path);
          }
      } else {
          exchange.setAttribute("request-path", "could not resolve request URI path");
          handleNotFound(exchange);
      }
  }

  private void handleServeFile(HttpExchange exchange, Path path) throws IOException {
     exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
     exchange.sendResponseHeaders(200, Files.size(path));
     try (InputStream fis = Files.newInputStream(path);
          OutputStream os = exchange.getResponseBody()) {
         fis.transferTo(os);
     }
  }

  private void handleNotFound(HttpExchange exchange) throws IOException {
    byte[] bytes = ("<p>Cannot find generated html file at: " +  exchange.getRequestURI().getPath() + "</p>\n").getBytes(UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

    exchange.sendResponseHeaders(404, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
    }
  }
}
