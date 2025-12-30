package com.nathanmcunha.minispring.context.interfaces;

import java.util.Optional;

public interface ApplicationContext {
  BeanFactory getBeanFactory();

  <T> Optional<T> getBean(Class<T> clazz);
}
