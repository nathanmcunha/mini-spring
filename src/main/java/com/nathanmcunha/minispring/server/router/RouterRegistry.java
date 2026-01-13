package com.nathanmcunha.minispring.server.router;

import com.nathanmcunha.minispring.annotations.Get;
import com.nathanmcunha.minispring.annotations.Post;
import com.nathanmcunha.minispring.annotations.Rest;
import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.registry.BeanFactory;
import com.nathanmcunha.minispring.error.FrameworkError;
import com.nathanmcunha.minispring.server.router.model.MethodHandler;
import com.nathanmcunha.minispring.server.router.model.RouteKey;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RouterRegistry implements Router {

  private final Map<RouteKey, MethodHandler> routes;

  private RouterRegistry(final Map<RouteKey, MethodHandler> routes) {
    this.routes = routes;
  }

  public static Result<RouterRegistry, FrameworkError> create(final BeanFactory factory) {
    return buildRouteRegistry(factory).map(RouterRegistry::new);
  }

  @Override
  public Result<MethodHandler, FrameworkError> getHandler(final String verb, final String path) {
    final RouteKey key = new RouteKey(verb, path);
    return Optional.ofNullable(routes.get(key))
        .map(Result::<MethodHandler, FrameworkError>success)
        .orElse(Result.failure(new FrameworkError.RouteNotFound(verb, path)));
  }

  private static Result<Map<RouteKey, MethodHandler>, FrameworkError> buildRouteRegistry(
      final BeanFactory factory) {

    final Map<RouteKey, MethodHandler> registry = new HashMap<>();
    var beans = factory.getBeansWithAnnotation(Rest.class);

    for (Class<?> clazz : beans) {
      // 1. Resolve the Bean Instance FIRST.
      // If the bean is missing, we fail the whole registry process immediately.
      var beanResult = factory.getBean(clazz);
      if (beanResult.isEmpty()) {
        return Result.failure(new FrameworkError.ControllerBeanNotFound(clazz));
      }
      Object instance = beanResult.get();

      // 2. Scan methods for that bean instance
      for (Method method : clazz.getDeclaredMethods()) {
        // Try to register GET
        var error = registerRoute(registry, instance, method, Get.class, "GET", Get::value);
        if (error != null) {
          return Result.failure(error);
        }

        // Try to register POST
        error = registerRoute(registry, instance, method, Post.class, "POST", Post::value);
        if (error != null) {
          return Result.failure(error);
        }
      }
    }

    return Result.success(registry);
  }

  /**
   * Helper to check for an annotation and register the route. Returns generic FrameworkError if
   * collision occurs, null if success/ignored.
   */
  private static <A extends Annotation> FrameworkError registerRoute(
      Map<RouteKey, MethodHandler> registry,
      Object bean,
      Method method,
      Class<A> annotationType,
      String verb,
      Function<A, String> pathExtractor) {

    if (!method.isAnnotationPresent(annotationType)) {
      return null;
    }

    String path = pathExtractor.apply(method.getAnnotation(annotationType));
    RouteKey key = new RouteKey(verb, path);

    if (registry.containsKey(key)) {
      MethodHandler existing = registry.get(key);
      return new FrameworkError.RouteCollision(
          verb, path, existing.method().getName(), method.getName());
    }

    registry.put(key, new MethodHandler(bean, method));
    return null;
  }
}
