package com.nathanmcunha.minispring.context.interfaces;

import java.util.Set;

public interface ApplicationContext {

  <T> T getBean(Class<T> clazz);

  <T> boolean containsBean(String name, Class<T> clazz);

  Set<Class<?>> getComponentsClasses();
}
