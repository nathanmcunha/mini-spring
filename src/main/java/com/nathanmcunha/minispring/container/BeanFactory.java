package com.nathanmcunha.minispring.container;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

public interface BeanFactory {
  void registerBean(Class<?> clazz, Object instance);

  <T> Optional<T> getBean(Class<T> clazz);

  boolean containsBean(Class<?> clazz);

  Collection<Class<?>> getBeansWithAnnotation(Class<? extends Annotation> annotationType);
}
