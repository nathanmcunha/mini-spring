package com.nathanmcunha.minispring.server;

import com.nathanmcunha.minispring.annotation.Get;
import com.nathanmcunha.minispring.annotation.Post;
import com.nathanmcunha.minispring.annotation.Rest;
import com.nathanmcunha.minispring.context.interfaces.ApplicationContext;
import com.nathanmcunha.minispring.server.interfaces.HandlerMapping;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RouteRegistry implements HandlerMapping {

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
