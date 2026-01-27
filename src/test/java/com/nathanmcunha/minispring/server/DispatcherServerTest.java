package com.nathanmcunha.minispring.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.boot.MiniApplicationContext;
import com.nathanmcunha.minispring.error.FrameworkError;
import com.nathanmcunha.minispring.server.dispatch.DispatcherServlet;
import com.nathanmcunha.minispring.server.router.Router;
import com.nathanmcunha.minispring.server.router.RouterRegistry;
import com.nathanmcunha.minispring.server.router.model.MethodHandler;
import com.nathanmcunha.minispring.server.test_components.rest.SimpleConfigRest;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DispatcherServerTest {

  @Mock private HttpExchange exchange;
  @Mock Router router;
  private DispatcherServlet servlet;

  @BeforeEach
  void setupContextAndRequest() {
    Result<MiniApplicationContext, FrameworkError> contextResult =
        MiniApplicationContext.boot(SimpleConfigRest.class);

    if (contextResult instanceof Result.Success<MiniApplicationContext, FrameworkError> success) {
      var context = success.value();
      var registryResult = RouterRegistry.create(context.getBeanFactory());

      if (registryResult instanceof Result.Success(var registry)) {
        this.servlet = new DispatcherServlet(registry);
      } else {
        throw new RuntimeException("Failed to create router registry for tests");
      }
    } else {
      throw new RuntimeException("Failed to boot context for tests");
    }
  }

  @Test
  void shouldReturnSimpleRestComponentInContext() throws URISyntaxException, IOException {
    when(exchange.getRequestURI()).thenReturn(new URI("/getTest"));
    when(exchange.getRequestMethod()).thenReturn("GET");
    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(exchange.getResponseBody()).thenReturn(responseBody);

    servlet.handle(exchange);
    assertEquals("ALO", responseBody.toString());
  }

  @Test
  void shouldReturnResponseTypeWithString() throws URISyntaxException, IOException {

    when(exchange.getRequestURI()).thenReturn(URI.create("/getResponse"));
    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(exchange.getResponseBody()).thenReturn(responseBody);
    when(exchange.getRequestMethod()).thenReturn("GET");
    servlet.handle(exchange);
    assertEquals("TestResponse", responseBody.toString());
    verify(exchange).sendResponseHeaders(eq(200), anyLong());
  }

  @Test
  void Dispatchwhenroutenotfound_Return404() throws IOException {
    servlet = new DispatcherServlet(router);

    when(exchange.getRequestURI()).thenReturn(URI.create("/unknown"));
    when(exchange.getRequestMethod()).thenReturn("GET");

    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(exchange.getResponseBody()).thenReturn(responseBody);

    when(router.getHandler("GET", "/unknown"))
        .thenReturn(
            Result.<MethodHandler, FrameworkError>failure(
                new FrameworkError.RouteNotFound("GET", "/unknown")));

    servlet.handle(exchange);

    verify(exchange).sendResponseHeaders(eq(404), anyLong());
  }

  @Test
  void dispatchWhenControllerThrows_Returns500()
      throws NoSuchMethodException, SecurityException, IOException {

    servlet = new DispatcherServlet(router);

    when(exchange.getRequestURI()).thenReturn(URI.create("/error"));

    when(exchange.getRequestMethod()).thenReturn("GET");

    // Point the handler to our failingMethod

    MethodHandler handler = new MethodHandler(this, this.getClass().getMethod("failingMethod"));

    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

    when(exchange.getResponseBody()).thenReturn(responseBody);

    when(router.getHandler("GET", "/error")).thenReturn(Result.success(handler));

    servlet.handle(exchange);

    verify(exchange).sendResponseHeaders(eq(500), anyLong());
  }

  @Test
  void dispatchWhenMethodIsPrivate_Returns500()
      throws NoSuchMethodException, SecurityException, IOException {
    servlet = new DispatcherServlet(router);

    when(exchange.getRequestURI()).thenReturn(URI.create("/private"));
    when(exchange.getRequestMethod()).thenReturn("GET");

    // Point to private method
    MethodHandler handler =
        new MethodHandler(this, this.getClass().getDeclaredMethod("privateMethod"));

    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(exchange.getResponseBody()).thenReturn(responseBody);
    when(router.getHandler("GET", "/private")).thenReturn(Result.success(handler));

    servlet.handle(exchange);

    verify(exchange).sendResponseHeaders(eq(500), anyLong());
  }

  @Test
  void handleErrorWhenGenericError_Returns500() throws Exception {
    FrameworkError genericError = new FrameworkError.InvalidRouteDefinition("Generic Error");

    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(exchange.getResponseBody()).thenReturn(responseBody);

    // 3. Use Reflection to invoke private handleError
    Method handleErrorMethod =
        DispatcherServlet.class.getDeclaredMethod(
            "handleError", HttpExchange.class, FrameworkError.class);
    handleErrorMethod.setAccessible(true);

    servlet = new DispatcherServlet(router);
    handleErrorMethod.invoke(servlet, exchange, genericError);

    verify(exchange).sendResponseHeaders(eq(500), anyLong());
    assertEquals("Error: " + genericError, responseBody.toString());
  }

  /** Method to simulate nested exception */
  public void nestedFailingMethod() {
    throw new RuntimeException("Outer", new Exception("Inner"));
  }

  @Test
  void dispatchWhenNestedException_Returns500()
      throws NoSuchMethodException, SecurityException, IOException {
    servlet = new DispatcherServlet(router);

    when(exchange.getRequestURI()).thenReturn(URI.create("/nested"));
    when(exchange.getRequestMethod()).thenReturn("GET");

    MethodHandler handler =
        new MethodHandler(this, this.getClass().getMethod("nestedFailingMethod"));

    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(exchange.getResponseBody()).thenReturn(responseBody);
    when(router.getHandler("GET", "/nested")).thenReturn(Result.success(handler));

    servlet.handle(exchange);

    verify(exchange).sendResponseHeaders(eq(500), anyLong());
  }

  /** Method used to simulate a controller failure */
  public void failingMethod() {

    throw new RuntimeException("Simulated controller failure");
  }

  /** Private method to simulate IllegalAccessException */
  private void privateMethod() {}
}
