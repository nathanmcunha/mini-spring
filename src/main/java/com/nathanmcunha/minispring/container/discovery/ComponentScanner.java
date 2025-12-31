package com.nathanmcunha.minispring.container.discovery;

import com.nathanmcunha.minispring.annotations.Component;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class ComponentScanner {

  private final Set<Class<?>> componentClasses = new HashSet<>();

  public Set<Class<?>> scanPackage(String packageName) throws IOException, ClassNotFoundException {
    String path = packageName.replace(".", "/");
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> resources = classLoader.getResources(path);
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      File directory = new File(resource.getFile());
      findClasses(directory, packageName);
    }
    return componentClasses;
  }

  private void findClasses(File directory, String packageName) throws ClassNotFoundException {
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

  public Set<Class<?>> getComponentsClasses() {
    return this.componentClasses;
  }
}
