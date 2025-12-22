package com.nathanmcunha.minispring.context;

import com.nathanmcunha.minispring.context.interfaces.ApplicationContext;
import com.nathanmcunha.minispring.context.scanners.ComponentScanner;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationContextConfig implements ApplicationContext {

  private final Map<Class<?>, Object> beanRegistry = new HashMap<>();
  private final Set<Class<?>> classesInCreation = new HashSet<>();

  public ApplicationContextConfig(final Class<?> configClass)
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException,
          ClassNotFoundException,
          IOException {
    final String basePackage = configClass.getPackageName();
    final ComponentScanner scanner = new ComponentScanner();
    instantiateBeans(scanner.scanPackage(basePackage));
  }

  private void instantiateBeans(final Set<Class<?>> componentClasses)
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {

    List<Class<?>> classesToInstantiate = new ArrayList<>(componentClasses);

    while (!classesToInstantiate.isEmpty()) {
      boolean beanCreatedInThisPass = false;
      final List<Class<?>> classesNotYetInstantiated = new ArrayList<>();

      for (final Class<?> clazz : classesToInstantiate) {
        if (beanRegistry.containsKey(clazz)) {
          continue;
        }

        // Handle circular dependencies
        if (classesInCreation.contains(clazz)) {
          classesNotYetInstantiated.add(clazz);
          continue;
        }

        try {

          classesInCreation.add(clazz);

          final Object instance = createBeanInstance(clazz);
          beanRegistry.put(clazz, instance);
          beanCreatedInThisPass = true;
          classesInCreation.remove(clazz);
        } catch (NoClassDefFoundError | NoSuchMethodException e) {
          classesNotYetInstantiated.add(clazz);
          classesInCreation.remove(clazz);
        } catch (final Exception e) {
          classesInCreation.remove(clazz);
          throw e;
        }
      }

      if (!beanCreatedInThisPass && !classesNotYetInstantiated.isEmpty()) {
        throw new IllegalStateException(
            "Unable to resolve all bean dependencies. Remaining classes: "
                + classesNotYetInstantiated);
      }
      classesToInstantiate = classesNotYetInstantiated;
    }
  }

  private Object createBeanInstance(final Class<?> clazz)
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {
    final var constructors = clazz.getDeclaredConstructors();
    if (constructors.length == 0) {
      throw new NoSuchMethodException("No constructor found for " + clazz.getName());
    }

    final var constructor = constructors[0];

    final var parameters = constructor.getParameters();
    final var args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
      final Class<?> paramType = parameters[i].getType();
      if (!beanRegistry.containsKey(paramType)) {
        throw new NoClassDefFoundError(
            "Dependency " + paramType.getName() + " not yet available for " + clazz.getName());
      }
      args[i] = beanRegistry.get(paramType);
    }

    return constructor.newInstance(args);
  }

  public <T> T getBean(final Class<T> clazz) {
    if (!beanRegistry.containsKey(clazz)) {
      throw new IllegalStateException(
          "Bean of type " + clazz.getName() + " not found in the application context.");
    }
    return (T) beanRegistry.get(clazz);
  }

  @Override
  public Set<Class<?>> getComponentePerAnnotationType(final Class<? extends Annotation> annotationType) {
    return beanRegistry.keySet().stream()
        .filter(clazz -> ApplicationContext.isClassTypeOf(clazz, annotationType))
        .collect(Collectors.toSet());
  }

  @Override
  public <T> boolean containsBean(final String name, final Class<T> clazz) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'containsBean'");
  }
}
