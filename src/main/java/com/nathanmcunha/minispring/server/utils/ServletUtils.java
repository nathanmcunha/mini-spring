package com.nathanmcunha.minispring.server.utils;

import com.nathanmcunha.minispring.annotation.Get;
import com.nathanmcunha.minispring.annotation.Rest;
import com.nathanmcunha.minispring.context.interfaces.ApplicationContext;
import com.nathanmcunha.minispring.server.MethodHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServletUtils {

  public static Map<String, MethodHandler> getRoutes(final ApplicationContext context) {
    return context.getComponentePerAnnotationType(Rest.class).stream()
        .flatMap(clazz -> extractRoutes(context, clazz))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Stream<Entry<String, MethodHandler>> extractRoutes(
      final ApplicationContext context, final Class<?> clazz) {
    {
      return Arrays.stream(clazz.getDeclaredMethods())
          .filter(method -> method.isAnnotationPresent(Get.class))
          .map(
              method -> {
                final String url = method.getAnnotation(Get.class).value();
                return Map.entry(url, generateMethodHandler(clazz, method, context));
              });
    }
  }

  private static MethodHandler generateMethodHandler(
      final Class<?> clazz, final Method method, final ApplicationContext context) {
    final Object beanInstance = context.getBean(clazz);
    return new MethodHandler(beanInstance, method);
  }
}
