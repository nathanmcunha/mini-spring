package com.nathanmcunha.minispring.server.router;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.nathanmcunha.minispring.annotations.Get;
import com.nathanmcunha.minispring.annotations.Post;
import com.nathanmcunha.minispring.annotations.Rest;
import com.nathanmcunha.minispring.container.ApplicationContext;
import com.nathanmcunha.minispring.server.router.model.MethodHandler;
import com.nathanmcunha.minispring.server.router.model.RouteKey;

/*
  Responsible to build the registry and put the beand into the context
  Also, extract and create the routes from the Rest.class and checking the type of the
  call e.g: GET.class and POST.class
**/
public class RouteRegistry implements Router {

  private final Map<RouteKey, MethodHandler> routes;

  public RouteRegistry(final ApplicationContext context) {
    routes = buildRegistry(context);
  }

  @Override
  public Optional<MethodHandler> getHandler(final String verb, final String path) {
    final RouteKey key = new RouteKey(verb, path);
    return Optional.ofNullable(routes.get(key));
  }

  private Map<RouteKey, MethodHandler> buildRegistry(final ApplicationContext context) {
    return context.getBeanFactory().getBeansWithAnnotation(Rest.class).stream()
        .flatMap(clazz -> extractRoutes(context, clazz))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private Stream<Entry<RouteKey, MethodHandler>> extractRoutes(
      final ApplicationContext context, final Class<?> clazz) {
    return Arrays.stream(clazz.getDeclaredMethods())
        .flatMap(
            method ->
                Stream.concat(
                    extractMethods(context, method, Get.class, "GET", Get::value),
                    extractMethods(context, method, Post.class, "POST", Post::value)));
  }

  private <A extends Annotation> Stream<Map.Entry<RouteKey, MethodHandler>> extractMethods(
      ApplicationContext context,
      Method method,
      Class<A> annotationType,
      String verb,
      Function<A, String> pathExtractor) {

    return Optional.ofNullable(method.getAnnotation(annotationType))
        .map(annotation -> createRouteEntry(context, method, verb, pathExtractor.apply(annotation)))
        .stream();
  }

  private Map.Entry<RouteKey, MethodHandler> createRouteEntry(
      ApplicationContext context, Method method, String verb, String path) {
    RouteKey key = new RouteKey(verb, path);
    Object bean =
        context
            .getBean(method.getDeclaringClass())
            .orElseThrow(
                () -> new RuntimeException("Bean not found: " + method.getDeclaringClass()));
    return Map.entry(key, new MethodHandler(bean, method));
  }
}
