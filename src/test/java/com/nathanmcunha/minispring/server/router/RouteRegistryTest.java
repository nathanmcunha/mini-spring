package com.nathanmcunha.minispring.server.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nathanmcunha.minispring.annotations.Get;
import com.nathanmcunha.minispring.annotations.Rest;
import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.registry.BeanFactory;
import com.nathanmcunha.minispring.error.FrameworkError;
import com.nathanmcunha.minispring.server.dispatch.protocol.Response;
import com.nathanmcunha.minispring.server.router.model.MethodHandler;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RouteRegistryTest {

  @Test
  public void shouldFailOnDuplicateRoutes() {
    BeanFactory factory = mock(BeanFactory.class);
    when(factory.getBeansWithAnnotation(Rest.class))
        .thenReturn(List.of(ControllerA.class, ControllerB.class));

    when(factory.getBean(ControllerA.class)).thenReturn(Optional.of(new ControllerA()));
    when(factory.getBean(ControllerB.class)).thenReturn(Optional.of(new ControllerB()));

    var result = RouterRegistry.create(factory);
    // 3. Assert Failure
    assertTrue(result instanceof Result.Failure);
    var error = ((Result.Failure<?, FrameworkError>) result).error();
    System.out.println(error);

    // 4. Validate Error Type
    assertTrue(error instanceof FrameworkError.RouteCollision);
    var collision = (FrameworkError.RouteCollision) error;
    assertEquals("GET", collision.verb());
    assertEquals("/conflict", collision.path());
  }

  @Test
  void shouldSuccessfullyRegisterRoutes() {
    BeanFactory factory = mock(BeanFactory.class);

    @Rest
    class HappyController {
      @Get("/test")
      public Response<String> handle() {
        return Response.Builder(200).body("ok");
      }
    }

    when(factory.getBeansWithAnnotation(Rest.class)).thenReturn(List.of(HappyController.class));
    when(factory.getBean(HappyController.class)).thenReturn(Optional.of(new HappyController()));

    var result = RouterRegistry.create(factory);

    assertTrue(
        result instanceof Result.Success<RouterRegistry, FrameworkError>,
        "Expected successful registration");

    var registry = ((Result.Success<RouterRegistry, FrameworkError>) result).value();
    Result<MethodHandler, FrameworkError> handler = registry.getHandler("GET", "/test");

    assertTrue(handler instanceof Result.Success);
    var successHandler = (Result.Success<MethodHandler, FrameworkError>) handler;

    assertEquals(HappyController.class, successHandler.value().instance().getClass());
  }

  @Test
  void shouldFailWhenControllerBeanIsMissing() {
    BeanFactory factory = mock(BeanFactory.class);

    @Rest
    class MissingController {
      @Get("/missing")
      public void handle() {}
    }

    // The class is found by the scanner...
    when(factory.getBeansWithAnnotation(Rest.class)).thenReturn(List.of(MissingController.class));

    // ...but the factory cannot provide an instance (simulating a DI failure)
    when(factory.getBean(MissingController.class)).thenReturn(Optional.empty());

    var result = RouterRegistry.create(factory);

    assertTrue(
        result instanceof Result.Failure<RouterRegistry, FrameworkError>,
        "Expected failure due to missing bean");

    var error = ((Result.Failure<RouterRegistry, FrameworkError>) result).error();
    assertTrue(error instanceof FrameworkError.ControllerBeanNotFound);
    assertEquals(
        MissingController.class, ((FrameworkError.ControllerBeanNotFound) error).beanClass());
  }
}
