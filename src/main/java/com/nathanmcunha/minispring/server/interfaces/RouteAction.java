package com.nathanmcunha.minispring.server.interfaces;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

@FunctionalInterface
public interface RouteAction {

  /**
   * Performs the action (e.g., executing a handler or sending a 404
   *
   * @param exchange the http to act
   * @throws IOException if and I/O error occurs during action
   */
  void perform(HttpExchange exchange) throws IOException;
}
