package com.nathanmcunha.minispring;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

public class MiniSpringApp {
  /**
   * @param args
   */
  public static void main(String[] args) {
    int port = 8080;
    try {
      var server = HttpServer.create(new InetSocketAddress(port), 0);
      server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
      System.out.println("Framework Started");
      server.start();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
