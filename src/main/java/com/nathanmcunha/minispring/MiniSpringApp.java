package com.nathanmcunha.minispring;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.boot.MiniApplicationContext;
import com.nathanmcunha.minispring.error.FrameworkError;
import com.nathanmcunha.minispring.server.dispatch.DispatcherServlet;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class MiniSpringApp {

  public static void main(String[] args) {
    // 1. Start the Server immediately (Listening but Empty)
    HttpServer server = startServer(8080);

    System.out.println("🌱 Booting MiniSpring Context...");

    // 2. Boot the Application Context
    var bootResult = MiniApplicationContext.boot(MiniSpringApp.class);

    // 3. Handle Result: Wire the app if success, kill server if fail
    switch (bootResult) {
      case Result.Success<MiniApplicationContext, FrameworkError>(var context) -> {
        var dispatcher = new DispatcherServlet(context.getRouterRegistry());
        // Dynamically add the handler to the running server
        server.createContext("/", dispatcher);
        System.out.println("✅ Application Context Wired. Ready to serve!");
      }
      case Result.Failure<MiniApplicationContext, FrameworkError>(var error) -> {
        System.err.println("🔥 Application Failed to Start!");
        System.err.println("Error: " + error);
        server.stop(0); // Stop the server immediately
        System.exit(1);
      }
    }
  }

  private static HttpServer startServer(int port) {
    try {
      var server = HttpServer.create(new InetSocketAddress(port), 0);
      server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
      server.start();
      System.out.println("🚀 Server started on port " + port);
      return server;
    } catch (IOException e) {
      System.err.println("❌ Failed to bind port " + port);
      throw new RuntimeException(e);
    }
  }
}