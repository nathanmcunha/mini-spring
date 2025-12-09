package com.nathanmcunha.minispring.context;

import com.nathanmcunha.minispring.annotation.Component;
import java.io.File;
import java.io.IOException;
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

  public void MiniApplicationContext(Class<?> configClass)
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
        if (clazz.isAnnotationPresent(Component.class)) {
          componentClasses.add(clazz);
        }
      }
    }
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
          // This means we have a circular dependency or some other complex scenario,
          // or we're just waiting for another bean. For now, defer it.
          classesNotYetInstantiated.add(clazz);
          continue;
        }

        try {
          classesInCreation.add(clazz); // Mark as being created

          Object instance = createBeanInstance(clazz); // Use helper method
          beanRegistry.put(clazz, instance);
          beanCreatedInThisPass = true;
          classesInCreation.remove(clazz); // Remove from in-creation list
        } catch (NoClassDefFoundError | NoSuchMethodException e) {
          // Dependency not found or constructor not resolvable yet, defer instantiation
          classesNotYetInstantiated.add(clazz);
          classesInCreation.remove(clazz); // Remove from in-creation as it failed
        } catch (Exception e) {
          classesInCreation.remove(clazz); // Remove from in-creation as it failed
          throw e; // Re-throw other exceptions
        }
      }

      if (!beanCreatedInThisPass && !classesNotYetInstantiated.isEmpty()) {
        // No beans were created in this pass, but some are still pending.
        // This indicates a missing dependency or a circular dependency that cannot be resolved.
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

    // For simplicity, we'll try to use the first declared constructor
    var constructor = constructors[0];

    var parameters = constructor.getParameters();
    var args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
      Class<?> paramType = parameters[i].getType();
      if (!beanRegistry.containsKey(paramType)) {
        // Dependency not yet available, this will cause a deferral
        throw new NoClassDefFoundError(
            "Dependency " + paramType.getName() + " not yet available for " + clazz.getName());
      }
      args[i] = beanRegistry.get(paramType); // Retrieve dependency from registry
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
}
