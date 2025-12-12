package com.nathanmcunha.minispring.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.nathanmcunha.minispring.context.ApplicationContext;
import com.nathanmcunha.minispring.server.test_components.rest.SimpleConfigRest;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DispatcherServerTest {

  @Mock private HttpExchange exchange;

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

    ApplicationContext context = new ApplicationContext();
    context.ApplicationContext(SimpleConfigRest.class);

    DispatcherServlet servlet = new DispatcherServlet(context);
    when(exchange.getRequestURI()).thenReturn(new URI("/getTest"));

    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(exchange.getResponseBody()).thenReturn(responseBody);

    servlet.handle(exchange);
    assertEquals("ALO", responseBody.toString());
  }
}
