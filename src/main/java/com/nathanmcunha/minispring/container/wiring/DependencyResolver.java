package com.nathanmcunha.minispring.container.wiring;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.nathanmcunha.minispring.container.metadata.BeanDefinition;

/*
  Responsible to do the wire and check the dependecies between beans
  e.g: If a Rest class depends a Service class this will be resolved here
**/
public class DependencyResolver {
  private final Map<Class<?>, Object> builtBeans = new HashMap<>();
  private final Set<Class<?>> currentlyBuilding = new HashSet<>();

  public DependencyResolver() {}

  public Map<Class<?>, Object> resolve(Set<BeanDefinition> definitions) {
    for (BeanDefinition beanDefinition : definitions) {
      createBean(beanDefinition.clazz(), definitions);
    }
    return Map.copyOf(builtBeans);
  }

    
  private Object createBean(Class<?> clazz, Set<BeanDefinition> allBeanDefitions) {
    if (builtBeans.containsKey(clazz)) {
      return builtBeans.get(clazz);
    }
    //remove this logic throwing a exception
    //Use Optional?
    if (currentlyBuilding.contains(clazz)) {
      throw new IllegalStateException("Circular Dependency: " + clazz.getName());
    }
    currentlyBuilding.add(clazz);
    try {
      Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
      Object[] args =
          Arrays.stream(constructor.getParameters())
              .map(param -> createBean(param.getType(), allBeanDefitions))
              .toArray();
      Object instance = instantiate(constructor, args);
      builtBeans.put(clazz, instance);
      return instance;
    } finally {
      currentlyBuilding.remove(clazz);
    }
  }

  private Object instantiate(Constructor<?> constructor, Object[] args) {
    try {
      return constructor.newInstance(args);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to build bean:" + constructor.getDeclaringClass().getName(), e);
    }
  }
}
