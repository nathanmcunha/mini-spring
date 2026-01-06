package com.nathanmcunha.minispring.server.router;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.ServerError;
import com.nathanmcunha.minispring.server.protocol.Response;
import com.sun.net.httpserver.HttpExchange;

@FunctionalInterface
public interface RouteAction {

  /**
   * Performs the action (e.g., executing a handler or sending a 404
   *
   * @param exchange the http to act
   */
  Result<Response<?>, ServerError> perform(HttpExchange exchange);
}
