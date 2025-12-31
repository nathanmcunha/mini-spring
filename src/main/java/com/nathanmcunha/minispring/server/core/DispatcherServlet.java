package com.nathanmcunha.minispring.server.core;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.server.router.HandlerMapping;
import com.nathanmcunha.minispring.server.router.MethodHandler;
import com.nathanmcunha.minispring.server.router.RouteAction;
import com.nathanmcunha.minispring.server.router.RouteError;
import com.nathanmcunha.minispring.server.model.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class DispatcherServlet implements HttpHandler {

  private final HandlerMapping handlerMapping;

  public DispatcherServlet(final HandlerMapping handlerMapping) {
    this.handlerMapping = handlerMapping;
  }

  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    String verb = exchange.getRequestMethod();
    String path = exchange.getRequestURI().getPath();
    
    Result<Response<?>, RouteError> result = handlerMapping
        .getHandler(verb, path)
        .map(this::createExecutionAction)
        .orElseGet(this::create404Action)
        .perform(exchange);

    if (result instanceof Result.Success<Response<?>, RouteError> success) {
        writeResponse(exchange, success.value());
    } else if (result instanceof Result.Failure<Response<?>, RouteError> failure) {
        handleError(exchange, failure.error());
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

  private void handleError(HttpExchange exchange, RouteError error) throws IOException {
      String message = "Error: " + error.message();
      byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(error.statusCode(), responseBytes.length);
      try (OutputStream responseBody = exchange.getResponseBody()) {
          responseBody.write(responseBytes);
      }
      if (error.cause() != null) {
          error.cause().printStackTrace();
      }
  }

  private RouteAction createExecutionAction(MethodHandler handler) {
    return (exchange) -> {
      try {
        var result = handler.method().invoke(handler.instance());
        Response<?> response;
        if (result instanceof Response<?> r) {
             response = r;
        } else {
             response = Response.Builder(HttpURLConnection.HTTP_OK).body(result);
        }
        return Result.success(response);
      } catch (InvocationTargetException | IllegalAccessException e) {
        return Result.failure(new RouteError(500, "Internal Server Error", e));
      }
    };
  }

  private RouteAction create404Action() {
    return (exchange) -> Result.success(Response.Builder(HttpURLConnection.HTTP_NOT_FOUND).build());
  }
}