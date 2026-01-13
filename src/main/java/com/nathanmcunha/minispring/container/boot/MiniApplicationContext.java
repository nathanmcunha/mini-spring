package com.nathanmcunha.minispring.container.boot;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.discovery.ComponentScannerReader;
import com.nathanmcunha.minispring.container.registry.BeanFactory;
import com.nathanmcunha.minispring.container.registry.DefaultBeanFactory;
import com.nathanmcunha.minispring.container.wiring.DependencyResolver;
import com.nathanmcunha.minispring.error.FrameworkError;
import com.nathanmcunha.minispring.server.router.RouterRegistry;
import java.util.Optional;

public class MiniApplicationContext implements ApplicationContext {

  private final BeanFactory factory;
  private final RouterRegistry routerRegistry;

  private MiniApplicationContext(BeanFactory validFactory, RouterRegistry routerRegistry) {
    this.factory = validFactory;
    this.routerRegistry = routerRegistry;
  }

  public static Result<MiniApplicationContext, FrameworkError> boot(Class<?> config) {
    return buildFactory(config)
        .flatMap(
            factory ->
                RouterRegistry.create(factory)
                    .map(router -> new MiniApplicationContext(factory, router)));
  }

  private static Result<BeanFactory, FrameworkError> buildFactory(Class<?> config) {
    return new ComponentScannerReader()
        .scan(config)
        .flatMap(definitions -> new DependencyResolver().resolve(definitions))
        .flatMap(beans -> new DefaultBeanFactory().registerBeans(beans));
  }

  @Override
  public <T> Optional<T> getBean(Class<T> clazz) {
    return factory.getBean(clazz);
  }

  @Override
  public BeanFactory getBeanFactory() {
    return this.factory;
  }

  public RouterRegistry getRouterRegistry() {
    return routerRegistry;
  }
}
