package com.nathanmcunha.minispring.container.wiring;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.metadata.BeanDefinition;
import com.nathanmcunha.minispring.error.FrameworkError;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orchestrates the instantiation and wiring of beans based on their definitions.
 *
 * <p>This resolver performs a recursive depth-first resolution of dependencies, ensuring that all
 * required arguments for a bean's constructor are instantiated and available before the bean itself
 * is created.
 *
 * <p>It also includes safeguards against circular dependencies.
 */
public class DependencyResolver {
  private final Map<Class<?>, Object> builtBeans = new HashMap<>();
  private final Set<Class<?>> currentlyBuilding = new HashSet<>();

  public DependencyResolver() {}

  public Result<Map<Class<?>, Object>, FrameworkError> resolve(Set<BeanDefinition> definitions) {
    for (BeanDefinition beanDefinition : definitions) {
      Result<Object, FrameworkError> result = resolveBean(beanDefinition.clazz(), definitions);
      if (result instanceof Result.Failure<Object, FrameworkError> failure) {
        return Result.failure(failure.error());
      }
    }
    return Result.success(Map.copyOf(builtBeans));
  }

  private Result<Object, FrameworkError> resolveBean(
      Class<?> type, Set<BeanDefinition> definitions) {
    return findImplementation(type, definitions).flatMap(impl -> createInstance(impl, definitions));
  }

  private Result<Class<?>, FrameworkError> findImplementation(
      Class<?> type, Set<BeanDefinition> definitions) {
    List<Class<?>> candidates =
        definitions.stream()
            .map(BeanDefinition::clazz)
            .filter(type::isAssignableFrom)
            .collect(Collectors.toList());

    if (candidates.isEmpty()) {
      return Result.failure(
          new FrameworkError.MissingDependency(
              "No implementation found for required type: " + type.getName()));
    }

    if (candidates.size() > 1) {
      return Result.failure(
          new FrameworkError.AmbiguousDependency(
              "Multiple beans found for type " + type.getName() + ": " + candidates));
    }

    return Result.success(candidates.get(0));
  }

  private Result<Object, FrameworkError> createInstance(
      Class<?> type, Set<BeanDefinition> definitions) {
    if (builtBeans.containsKey(type)) {
      return Result.success(builtBeans.get(type));
    }

    if (currentlyBuilding.contains(type)) {
      return Result.failure(
          new FrameworkError.CircularDependencyDetected("Circular Dependency: " + type.getName()));
    }

    currentlyBuilding.add(type);
    try {
      Constructor<?>[] constructors = type.getDeclaredConstructors();
      if (constructors.length == 0) {
        return Result.failure(
            new FrameworkError.BeanInstantiationFailed(
                "No constructors found for " + type.getName(), null));
      }
      Constructor<?> constructor = constructors[0];
      return resolveArguments(constructor.getParameters(), definitions)
          .flatMap(args -> instantiate(constructor, args))
          .map(
              instance -> {
                builtBeans.put(type, instance);
                return instance;
              });
    } finally {
      currentlyBuilding.remove(type);
    }
  }

  private Result<Object[], FrameworkError> resolveArguments(
      Parameter[] parameters, Set<BeanDefinition> definitions) {
    List<Object> args = new ArrayList<>();
    for (Parameter param : parameters) {
      switch (resolveBean(param.getType(), definitions)) {
        case Result.Success<Object, FrameworkError>(var value) -> args.add(value);
        case Result.Failure<Object, FrameworkError>(var error) -> {
          return Result.failure(error);
        }
      }
    }
    return Result.success(args.toArray());
  }

  private Result<Object, FrameworkError> instantiate(Constructor<?> constructor, Object[] args) {
    try {
      return Result.success(constructor.newInstance(args));
    } catch (Exception e) {
      return Result.failure(
          new FrameworkError.BeanInstantiationFailed(
              "Failed to build bean:" + constructor.getDeclaringClass().getName(), e));
    }
  }
}
