package com.nathanmcunha.minispring.container.discovery;

import com.nathanmcunha.minispring.annotations.Component;
import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.ScanError;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class ComponentScanner {

  private final Set<Class<?>> componentClasses = new HashSet<>();

  public Result<Set<Class<?>>, ScanError> scanPackage(String packageName) {
    try {
      String path = packageName.replace(".", "/");
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Enumeration<URL> resources = classLoader.getResources(path);
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        File directory = new File(resource.getFile());
        Result<Void, ScanError> result = findClasses(directory, packageName);
        if (result instanceof Result.Failure<Void, ScanError> failure) {
          return Result.failure(failure.error());
        }
      }
      return Result.success(Set.copyOf(componentClasses));
    } catch (IOException | ClassNotFoundException e) {
      return Result.failure(new ScanError("Failed to scan package: " + packageName, e));
    }
  }

  private Result<Void, ScanError> findClasses(File directory, String packageName) throws ClassNotFoundException {
    if (!directory.exists()) {
      return Result.success(null);
    }
    File[] files = directory.listFiles();
    if (files == null) {
      return Result.success(null);
    }
    for (File file : files) {
      if (file.isDirectory()) {
        Result<Void, ScanError> result = findClasses(file, packageName.concat(".").concat(file.getName()));
        if (result instanceof Result.Failure<Void, ScanError> failure) {
          return Result.failure(failure.error());
        }
      } else if (file.getName().endsWith(".class")) {
        String className = packageName.concat(".").concat(file.getName().replace(".class", ""));

        Class<?> clazz = Class.forName(className);
        if (isComponent(clazz)) {
          componentClasses.add(clazz);
        }
      }
    }
    return Result.success(null);
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
    return Set.copyOf(this.componentClasses);
  }
}
