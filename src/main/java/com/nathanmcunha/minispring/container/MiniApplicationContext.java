package com.nathanmcunha.minispring.container;

import com.nathanmcunha.minispring.container.DefaultBeanFactory;
import com.nathanmcunha.minispring.container.DependencyResolver;
import com.nathanmcunha.minispring.container.ApplicationContext;
import com.nathanmcunha.minispring.container.BeanDefinitionReader;
import com.nathanmcunha.minispring.container.BeanFactory;
import com.nathanmcunha.minispring.container.scanners.ComponentScannerReader;
import java.util.Optional;

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
