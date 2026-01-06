package com.nathanmcunha.minispring.container.boot;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.ApplicationContext;
import com.nathanmcunha.minispring.container.BeanFactory;
import com.nathanmcunha.minispring.container.discovery.ComponentScannerReader;
import com.nathanmcunha.minispring.container.registry.DefaultBeanFactory;
import com.nathanmcunha.minispring.container.wiring.DependencyResolver;
import com.nathanmcunha.minispring.error.ContextError;
import java.util.Optional;

public class MiniApplicationContext implements ApplicationContext {

  private final BeanFactory factory;

  private MiniApplicationContext(BeanFactory validFactory) {
    this.factory = validFactory;
  }

  public static Result<MiniApplicationContext, ContextError> boot(Class<?> config) {
    return new ComponentScannerReader()
        // reader the files that will be resolved as dependency/wiring
        .scan(config)
        .mapError(e -> (ContextError) e)
        .flatMap(
            definitions ->
                // this is the wiring
                new DependencyResolver().resolve(definitions).mapError(e -> (ContextError) e))
        .map(
            beans -> {
              // this is the instantiations of the beans and registry
              var factory = new DefaultBeanFactory();
              beans.forEach(factory::registerBean);
              return new MiniApplicationContext(factory);
            });
  }

  @Override
  public <T> Optional<T> getBean(Class<T> clazz) {
    return factory.getBean(clazz);
  }

  @Override
  public BeanFactory getBeanFactory() {
    return this.factory;
  }
}
