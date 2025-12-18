package com.nathanmcunha.minispring.server;

import com.nathanmcunha.minispring.annotation.Get;
import com.nathanmcunha.minispring.annotation.Rest;
import com.nathanmcunha.minispring.context.interfaces.ApplicationContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DispatcherServlet implements HttpHandler {

  private final Map<String, MethodHandler> routes;

  public DispatcherServlet(final ApplicationContext context) {
    this.routes = scanForControllers(context);
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

      Object result = route.method.invoke(route.instance);
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

  private Map<String, MethodHandler> scanForControllers(ApplicationContext context) {
    var componentClasses = context.getComponentsClasses();
    return componentClasses.stream()
        .filter(this::isRestController)
        .flatMap(clazz -> this.extractRoutes(context, clazz))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Stream<Entry<String, MethodHandler>> extractRoutes(
      ApplicationContext context, Class<?> clazz) {
    {
      return Arrays.stream(clazz.getDeclaredMethods())
          .filter(method -> method.isAnnotationPresent(Get.class))
          .map(
              method -> {
                String url = method.getAnnotation(Get.class).value();
                return Map.entry(url, generateMethodHandler(clazz, method, context));
              });
    }
  }

  private boolean isRestController(Class<?> clazz) {
    return clazz.isAnnotationPresent(Rest.class);
  }

  private MethodHandler generateMethodHandler(
      Class<?> clazz, Method method, ApplicationContext context) {
    Object beanInstance = context.getBean(clazz);
    return new MethodHandler(beanInstance, method);
  }

  record MethodHandler(Object instance, Method method) {}
}
