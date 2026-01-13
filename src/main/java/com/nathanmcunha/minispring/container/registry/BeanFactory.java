package com.nathanmcunha.minispring.container.registry;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.FrameworkError;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BeanFactory {

  Result<BeanFactory, FrameworkError> registerBeans(Map<Class<?>, Object> beans);

  <T> Optional<T> getBean(Class<T> clazz);

  boolean containsBean(Class<?> clazz);

  List<Class<?>> getBeansWithAnnotation(Class<? extends Annotation> annotationType);
}
