package com.nathanmcunha.minispring.container.boot;

import com.nathanmcunha.minispring.container.registry.BeanFactory;
import com.nathanmcunha.minispring.server.router.RouterRegistry;
import java.util.Optional;

public interface ApplicationContext {
  BeanFactory getBeanFactory();

  <T> Optional<T> getBean(Class<T> clazz);

  RouterRegistry getRouterRegistry();
}
