package com.nathanmcunha.minispring.server.dispatch;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.ServerError;
import com.nathanmcunha.minispring.server.protocol.HttpStatus;
import com.nathanmcunha.minispring.server.protocol.Response;
import com.nathanmcunha.minispring.server.router.RouteAction;
import com.nathanmcunha.minispring.server.router.Router;
import com.nathanmcunha.minispring.server.router.model.MethodHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/*
  Responsible to receive the http from the server and handle
**/
public class DispatcherServlet implements HttpHandler {

  private final Router router;

  public DispatcherServlet(final Router router) {
    this.router = router;
  }

  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    String verb = exchange.getRequestMethod();
    String path = exchange.getRequestURI().getPath();

    Result<Response<?>, ServerError> result = router
        .getHandler(verb, path)
        .map(this::createExecutionAction)
        .orElseGet(this::create404Action)
        .perform(exchange);

    switch (result) {
      case Result.Success(var response) -> writeResponse(exchange, response);
      case Result.Failure(var error) -> handleError(exchange, error);
    }
  }

  private void writeResponse(HttpExchange exchange, Response<?> response) throws IOException {
    var bodyToSend = response.body() != null ? response.body() : "";
    byte[] responseBytes = bodyToSend.toString().getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(response.statusCode(), responseBytes.length);
    try (OutputStream responseBody = exchange.getResponseBody()) {
      responseBody.write(responseBytes);
    }
  }

  private void handleError(HttpExchange exchange, ServerError error) throws IOException {
    String message = "Error: " + error.exception();
    byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(error.suggestedStatusCode(), responseBytes.length);
    try (OutputStream responseBody = exchange.getResponseBody()) {
      responseBody.write(responseBytes);
    }
    if (error.exception().getCause() != null) {
      error.exception().getCause().printStackTrace();
    }
  }

  private RouteAction createExecutionAction(MethodHandler handler) {
    return (exchange) -> {
      try {
        var result = handler.method().invoke(handler.instance());
        if (result instanceof Response<?> response) {
          return Result.success(response);
        }
        return Result.success(Response.Builder(HttpStatus.OK.value()).body(result));
      } catch (InvocationTargetException e) {
        Exception cause = (Exception) e.getCause();
        return Result.failure(new ServerError(cause, HttpStatus.INTERNAL_SERVER_ERROR.value()));
      } catch (Exception e) {
        return Result.failure(new ServerError(e, HttpStatus.INTERNAL_SERVER_ERROR.value()));
      }
    };
  }

  private RouteAction create404Action() {
    return (exchange) -> Result.success(Response.Builder(HttpStatus.NOT_FOUND.value()).build());
  }
}
