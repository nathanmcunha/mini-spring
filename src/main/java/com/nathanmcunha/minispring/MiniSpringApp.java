package com.nathanmcunha.minispring;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class MiniSpringApp {
  public static void main(String[] args) {
    int port = 8080;
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

      server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
      System.out.println("Framework Started");
      server.start();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
