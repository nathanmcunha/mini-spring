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
import com.nathanmcunha.minispring.server.router.RouterRegistry;
import com.nathanmcunha.minispring.server.test_components.rest.SimpleConfigRest;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    when(exchange.getRequestURI()).thenReturn(new URI("/getResponse"));
    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(exchange.getResponseBody()).thenReturn(responseBody);
    when(exchange.getRequestMethod()).thenReturn("GET");

    servlet.handle(exchange);
    assertEquals("TestResponse", responseBody.toString());
    verify(exchange).sendResponseHeaders(eq(200), anyLong());
  }
}