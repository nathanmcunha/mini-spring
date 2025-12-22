package com.nathanmcunha.minispring.server;

import com.nathanmcunha.minispring.context.interfaces.ApplicationContext;
import com.nathanmcunha.minispring.server.utils.ServletUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DispatcherServlet implements HttpHandler {

  private final Map<String, MethodHandler> routes;

  public DispatcherServlet(final ApplicationContext context) {
       this.routes = ServletUtils.getRoutes(context);
  }

  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();
    MethodHandler route = routes.get(path);
    if (route == null) {
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1);
      return;
    }

    try {

      Object result = route.method().invoke(route.instance());
      var statusToSend = HttpURLConnection.HTTP_OK;
      Object bodyToSend = result;
      if (result instanceof Response<?> response) {
        statusToSend = response.statusCode();
        bodyToSend = response.body();
      }
      byte[] responseBytes = bodyToSend.toString().getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(statusToSend, responseBytes.length);
      OutputStream responseBody = exchange.getResponseBody();
      responseBody.write(responseBytes);
      responseBody.close();
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1);
    }
  }
}
