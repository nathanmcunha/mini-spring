package com.nathanmcunha.minispring.container.wiring;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.metadata.BeanDefinition;
import com.nathanmcunha.minispring.error.DependencyError;
import com.nathanmcunha.minispring.error.DependencyError.CircularDependencyFailed;
import com.nathanmcunha.minispring.error.DependencyError.InstantiationFailed;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
  Responsible to do the wire and check the dependecies between beans
  e.g: If a Rest class depends a Service class this will be resolved here
**/
public class DependencyResolver {
  private final Map<Class<?>, Object> builtBeans = new HashMap<>();
  private final Set<Class<?>> currentlyBuilding = new HashSet<>();

  public DependencyResolver() {}

  public Result<Map<Class<?>, Object>, DependencyError> resolve(Set<BeanDefinition> definitions) {
    for (BeanDefinition beanDefinition : definitions) {
      Result<Object, DependencyError> result = resolveBean(beanDefinition.clazz(), definitions);
      if (result instanceof Result.Failure<Object, DependencyError> failure) {
        return Result.failure(failure.error());
      }
    }
    return Result.success(Map.copyOf(builtBeans));
  }

  private Result<Object, DependencyError> resolveBean(
      Class<?> clazz, Set<BeanDefinition> allBeanDefinitions) {
    if (builtBeans.containsKey(clazz)) {
      return Result.success(builtBeans.get(clazz));
    }
    if (currentlyBuilding.contains(clazz)) {
      return Result.failure(
          new CircularDependencyFailed("Circular Dependency: " + clazz.getName()));
    }

    currentlyBuilding.add(clazz);
    try {
      Constructor<?> constructor = clazz.getDeclaredConstructors()[0];

      return resolveArguments(constructor.getParameters(), allBeanDefinitions)
          .flatMap(args -> instantiate(constructor, args))
          .map(
              instance -> {
                builtBeans.put(clazz, instance);
                return instance;
              });

    } finally {
      currentlyBuilding.remove(clazz);
    }
  }

  private Result<Object[], DependencyError> resolveArguments(
      Parameter[] parameters, Set<BeanDefinition> allBeanDefinitions) {
    List<Object> args = new ArrayList<>();
    for (java.lang.reflect.Parameter param : parameters) {
      Result<Object, DependencyError> result = resolveBean(param.getType(), allBeanDefinitions);
      switch (result) {
        case Result.Success<Object, DependencyError>(var value) -> args.add(value);
        case Result.Failure<Object, DependencyError>(var error) -> {
          return Result.failure(error);
        }
      }
    }
    return Result.success(args.toArray());
  }

  private Result<Object, DependencyError> instantiate(Constructor<?> constructor, Object[] args) {
    try {
      return Result.success(constructor.newInstance(args));
    } catch (Exception e) {
      return Result.failure(
          new InstantiationFailed(
              "Failed to build bean:" + constructor.getDeclaringClass().getName(), e));
    }
  }
}
