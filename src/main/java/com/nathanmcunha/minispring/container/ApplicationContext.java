package com.nathanmcunha.minispring.container;

import com.nathanmcunha.minispring.server.router.RouterRegistry;
import java.util.Optional;

public interface ApplicationContext {
  BeanFactory getBeanFactory();

  <T> Optional<T> getBean(Class<T> clazz);

  RouterRegistry getRouterRegistry();
}
