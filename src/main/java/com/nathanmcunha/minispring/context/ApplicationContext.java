package com.nathanmcunha.minispring.context;

import com.nathanmcunha.minispring.annotation.Component;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApplicationContext {

  private final Map<Class<?>, Object> beanRegistry = new HashMap<>();
  private final Set<Class<?>> componentClasses = new HashSet<>();
  private final Set<Class<?>> classesInCreation = new HashSet<>();

  public void ApplicationContext(final Class<?> configClass)
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {
    String basePackage = configClass.getPackageName();
    scanPackages(basePackage);
    instantiateBeans();
  }

  /**
   * @param packageName
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  private void scanPackages(String packageName)
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {
    try {

      String path = packageName.replace(".", "/");
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Enumeration<URL> resources = classLoader.getResources(path);

      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        File directory = new File(resource.getFile());
        findClasses(directory, packageName);
      }

    } catch (IOException | ClassNotFoundException e) {
      // TODO: handle exception
    }
  }

  private void findClasses(File directory, String packageName)
      throws ClassNotFoundException,
          InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {

    if (!directory.exists()) return;

    File[] files = directory.listFiles();

    for (File file : files) {
      if (file.isDirectory()) {
        findClasses(file, packageName.concat(".").concat(file.getName()));
      } else if (file.getName().endsWith(".class")) {
        String className = packageName.concat(".").concat(file.getName().replace(".class", ""));
        Class<?> clazz = Class.forName(className);
        if (isComponent(clazz)) {
          componentClasses.add(clazz);
        }
      }
    }
  }

  private boolean isComponent(Class<?> clazz) {
    if (clazz.isAnnotationPresent(Component.class)) {
      return true;
    }
    for (Annotation annotation : clazz.getAnnotations()) {
      if (annotation.annotationType().isAnnotationPresent(Component.class)) {
        return true;
      }
    }
    return false;
  }

  private void instantiateBeans()
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {

    List<Class<?>> classesToInstantiate = new ArrayList<>(componentClasses);

    while (!classesToInstantiate.isEmpty()) {
      boolean beanCreatedInThisPass = false;
      List<Class<?>> classesNotYetInstantiated = new ArrayList<>();

      for (Class<?> clazz : classesToInstantiate) {
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

          Object instance = createBeanInstance(clazz);
          beanRegistry.put(clazz, instance);
          beanCreatedInThisPass = true;
          classesInCreation.remove(clazz);
        } catch (NoClassDefFoundError | NoSuchMethodException e) {
          classesNotYetInstantiated.add(clazz);
          classesInCreation.remove(clazz);
        } catch (Exception e) {
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

  private Object createBeanInstance(Class<?> clazz)
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {
    var constructors = clazz.getDeclaredConstructors();
    if (constructors.length == 0) {
      throw new NoSuchMethodException("No constructor found for " + clazz.getName());
    }

    var constructor = constructors[0];

    var parameters = constructor.getParameters();
    var args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
      Class<?> paramType = parameters[i].getType();
      if (!beanRegistry.containsKey(paramType)) {
        throw new NoClassDefFoundError(
            "Dependency " + paramType.getName() + " not yet available for " + clazz.getName());
      }
      args[i] = beanRegistry.get(paramType);
    }

    return constructor.newInstance(args);
  }

  @SuppressWarnings("unchecked")
  public <T> T getBean(Class<T> clazz) {
    if (!beanRegistry.containsKey(clazz)) {
      throw new IllegalStateException(
          "Bean of type " + clazz.getName() + " not found in the application context.");
    }
    return (T) beanRegistry.get(clazz);
  }

  public Set<Class<?>> getComponentsClasses() {
    return this.componentClasses;
  }
}
