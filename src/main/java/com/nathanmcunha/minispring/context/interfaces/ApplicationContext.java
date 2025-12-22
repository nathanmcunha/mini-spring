package com.nathanmcunha.minispring.context.interfaces;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface ApplicationContext {

  <T> T getBean(Class<T> clazz);

  <T> boolean containsBean(String name, Class<T> clazz);

  Collection<Class<?>> getComponentePerAnnotationType(
      Class<? extends Annotation> annotationType);

  static boolean isClassTypeOf(Class<?> clazz, Class<? extends Annotation> annotationType) {
    return clazz.isAnnotationPresent(annotationType);
  }
}
