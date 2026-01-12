package com.nathanmcunha.minispring.container;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.FrameworkError;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public interface BeanFactory {
  Result<Void, FrameworkError> registerBean(Class<?> clazz, Object instance);

  <T> Optional<T> getBean(Class<T> clazz);

  boolean containsBean(Class<?> clazz);

  List<Class<?>> getBeansWithAnnotation(Class<? extends Annotation> annotationType);
}
