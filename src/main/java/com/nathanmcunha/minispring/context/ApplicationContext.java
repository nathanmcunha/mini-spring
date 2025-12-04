package com.nathanmcunha.minispring.context;

import com.nathanmcunha.minispring.annotation.Component;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationContext {

  private final Map<Class<?>, Object> beanRegistry = new HashMap<>();

  public void MiniApplicationContext(Class<?> configClass)
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {
    String basePackage = configClass.getPackageName();
    scanClasses(basePackage);
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
  private void scanClasses(String packageName)
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {
    try {

      // Getting package name from ConfigClass
      String path = packageName.replace(".", "/");
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Enumeration<URL> resources = classLoader.getResources(path);

      List<File> directories = new ArrayList<>();
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        directories.add(new File(resource.getFile()));
        for (File directory : directories) {
          findClasses(directory, packageName);
        }
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

        System.out.println(className);

        Class<?> clazz = Class.forName(className);
        if (clazz.isAnnotationPresent(Component.class)) {
          Object instance = clazz.getDeclaredConstructor().newInstance();
          beanRegistry.put(clazz, instance);
          System.out.println("Bean created");
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getBean(Class<T> clazz) {
    return (T) beanRegistry.get(clazz);
  }
}
