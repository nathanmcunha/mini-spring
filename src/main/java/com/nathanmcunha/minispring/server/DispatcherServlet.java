package com.nathanmcunha.minispring.server;

import com.nathanmcunha.minispring.server.interfaces.HandlerMapping;
import com.nathanmcunha.minispring.server.interfaces.RouteAction;
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
    handlerMapping
        .getHandler(verb, path)
        .map(this::createExecutionAction)
        .orElseGet(this::create404Action)
        .perform(exchange);
  }

  private RouteAction createExecutionAction(MethodHandler handler) {
    return (exchange) -> {
      try {
        var result = handler.method().invoke(handler.instance());
        var statusToSend = HttpURLConnection.HTTP_OK;
        var bodyToSend = result;
        if (result instanceof Response<?> response) {
          statusToSend = response.statusCode();
          bodyToSend = response.body();
        }

        byte[] responseBytes = bodyToSend.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusToSend, responseBytes.length);
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(responseBytes);
        responseBody.close();
      } catch (InvocationTargetException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private RouteAction create404Action() {
    return (exchange) -> {
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
      exchange.close();
    };
  }
}
