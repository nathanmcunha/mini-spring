package com.nathanmcunha.minispring.container.registry;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.FrameworkError;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link BeanFactory} that stores bean instances in a concurrent map.
 *
 * <p>This implementation provides thread-safe access to managed beans and supports
 * registration of a batch of beans at once.</p>
 */
public class DefaultBeanFactory implements BeanFactory {

  private final Map<Class<?>, Object> registry = new ConcurrentHashMap<>();

  @Override
  public Result<BeanFactory, FrameworkError> registerBeans(Map<Class<?>, Object> beans) {
    for (var bean : beans.entrySet()) {
      if (registry.putIfAbsent(bean.getKey(), bean.getValue()) != null) {
        return Result.failure(
            new FrameworkError.InvalidRouteDefinition(
                "Bean already registered: " + bean.getKey().getName()));
      }
    }

    return Result.success(this);
  }

  @Override
  public <T> Optional<T> getBean(Class<T> clazz) {

    Object bean = registry.get(clazz);
    // Safe Casting
    return Optional.ofNullable(clazz.cast(bean));
  }

  @Override
  public boolean containsBean(Class<?> clazz) {
    return registry.containsKey(clazz);
  }

  @Override
  public List<Class<?>> getBeansWithAnnotation(Class<? extends Annotation> type) {
    return registry.keySet().stream().filter(clazz -> isClassTypeOf(clazz, type)).toList();
  }

  static boolean isClassTypeOf(Class<?> clazz, Class<? extends Annotation> annotationType) {
    return clazz.isAnnotationPresent(annotationType);
  }
}
