package com.nathanmcunha.minispring;

import com.nathanmcunha.minispring.context.DefaultBeanFactory;
import com.nathanmcunha.minispring.context.DependencyResolver;
import com.nathanmcunha.minispring.context.interfaces.ApplicationContext;
import com.nathanmcunha.minispring.context.interfaces.BeanDefinitionReader;
import com.nathanmcunha.minispring.context.interfaces.BeanFactory;
import com.nathanmcunha.minispring.context.scanners.ComponentScannerReader;
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
