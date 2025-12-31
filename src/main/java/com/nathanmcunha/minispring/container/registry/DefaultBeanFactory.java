package com.nathanmcunha.minispring.container.registry;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.nathanmcunha.minispring.container.BeanFactory;

/*
This class is responsible to create a registry with the beans
and put in data structure
**/
public class DefaultBeanFactory implements BeanFactory {

  private final Map<Class<?>, Object> registry = new ConcurrentHashMap<>();

  @Override
  // change to not be void and return a result success or fail
  public void registerBean(Class<?> clazz, Object instance) {
    if (registry.containsKey(clazz)) {
      throw new IllegalStateException("Bean already defined: " + clazz.getName());
    }
    registry.put(clazz, instance);
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
  public Set<Class<?>> getBeansWithAnnotation(Class<? extends Annotation> type) {
    return registry.keySet().stream()
        .filter(clazz -> isClassTypeOf(clazz, type))
        .collect(Collectors.toSet());
  }

  static boolean isClassTypeOf(Class<?> clazz, Class<? extends Annotation> annotationType) {
    return clazz.isAnnotationPresent(annotationType);
  }
}
