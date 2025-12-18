package com.nathanmcunha.minispring.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nathanmcunha.minispring.context.ConfigApplicationContext;
import com.nathanmcunha.minispring.context.interfaces.ApplicationContext;
import com.nathanmcunha.minispring.server.test_components.rest.SimpleConfigRest;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
  void setupContextAndRequest()
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {
    ApplicationContext context = new ConfigApplicationContext(SimpleConfigRest.class);

    servlet = new DispatcherServlet(context);
  }

  @Test
  void shouldReturnSimpleRestComponentInContext()
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException,
          URISyntaxException,
          IOException {

    when(exchange.getRequestURI()).thenReturn(new URI("/getTest"));
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

    servlet.handle(exchange);
    assertEquals("TestResponse", responseBody.toString());
    verify(exchange).sendResponseHeaders(eq(200), anyLong());
  }
}
