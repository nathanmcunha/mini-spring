package com.nathanmcunha.minispring.container.scanners;

import com.nathanmcunha.minispring.annotations.Component;
import com.nathanmcunha.minispring.container.BeanDefinition;
import com.nathanmcunha.minispring.container.BeanDefinitionReader;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentScannerReader implements BeanDefinitionReader {

  public Set<BeanDefinition> scan(String packageName) {
    try {
      String path = packageName.replace(".", "/");
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Enumeration<URL> resources;

      resources = classLoader.getResources(path);
      var scanned = Collections.list(resources).stream()
          .flatMap(url -> scanDirectory(url, packageName))
          .collect(Collectors.toSet());
      return scanned;

    } catch (IOException e) {
        throw new RuntimeException("Circular???");
    }
  }

  private Stream<BeanDefinition> scanDirectory(URL resource, String packageName) {
    try {

      Path root = Paths.get(resource.toURI());
      return Files.walk(root)
          .filter(Files::isRegularFile)
          .filter(file -> file.toString().endsWith(".class"))
          .flatMap(
              file ->
                  loadClass(root, file, packageName).stream()
                      .filter(this::isComponent)
                      .map(
                          clazz ->
                              new BeanDefinition(
                                  clazz, clazz.getConstructors()[0].getParameterTypes())));
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException("Failed to scan package " + packageName, e);
    }
  }

  private Optional<Class<?>> loadClass(Path root, Path file, String basePackage) {
    String relative = root.relativize(file).toString();
    String className =
        basePackage.concat(".").concat(relative).replace(File.separator, ".").replace(".class", "");
    try {
      return Optional.of(Class.forName(className));
    } catch (ClassNotFoundException e) {
      return Optional.empty();
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
}
