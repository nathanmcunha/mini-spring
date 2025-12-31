package com.nathanmcunha.minispring.container.boot;

import java.util.Optional;
import com.nathanmcunha.minispring.container.ApplicationContext;
import com.nathanmcunha.minispring.container.BeanFactory;
import com.nathanmcunha.minispring.container.discovery.ComponentScannerReader;
import com.nathanmcunha.minispring.container.metadata.BeanDefinitionReader;
import com.nathanmcunha.minispring.container.registry.DefaultBeanFactory;
import com.nathanmcunha.minispring.container.wiring.DependencyResolver;

public class MiniApplicationContext implements ApplicationContext {

  private final BeanFactory factory;
  private final BeanDefinitionReader reader;

  public MiniApplicationContext(Class<?> config) {
    this.factory = new DefaultBeanFactory();
    this.reader = new ComponentScannerReader();
    var definitions = reader.scan(config.getPackageName());
    final var instances = new DependencyResolver().resolve(definitions);
    instances.forEach(factory::registerBean);
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
