package com.nathanmcunha.minispring.container.discovery;

import com.nathanmcunha.minispring.annotations.Component;
import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.metadata.BeanDefinition;
import com.nathanmcunha.minispring.container.metadata.BeanDefinitionReader;
import com.nathanmcunha.minispring.error.ScanError;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentScannerReader implements BeanDefinitionReader {

  public Result<Set<BeanDefinition>, ScanError> scan(Class<?> config) {
    try {
      String packageName = config.getPackageName();
      String path = packageName.replace(".", "/");
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      List<URL> resources = classLoader.resources(path).toList();
      return Result.traverse(
          resources,
          url -> scanRoot(url, packageName),
          Collectors.flatMapping(Set::stream, Collectors.toSet()));
    } catch (Exception e) {
      return Result.failure(new ScanError("Failed to scan resources", e));
    }
  }

  // this is responsible for I/O operation , basically is reading the folders looking for classes
  // to be mapped/discovery
  private Result<Set<BeanDefinition>, ScanError> scanRoot(URL resource, String packageName) {
    try {
      Path root = Paths.get(resource.toURI());

      Result<List<Path>, ScanError> filesResult = safeWalk(root);
      return filesResult.map(
          paths ->
              paths.stream()
                  .flatMap(file -> loadClass(root, file, packageName).stream())
                  .filter(this::isComponent)
                  .map(this::createBeanDefinition)
                  .collect(Collectors.toSet()));
    } catch (Exception e) {
      return Result.failure(new ScanError("Scan failed for root: " + resource, e));
    }
  }

  private Result<List<Path>, ScanError> safeWalk(Path root) {
    try (var stream = Files.walk(root)) {
      List<Path> files =
          stream.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".class")).toList();
      return Result.success(files);
    } catch (IOException e) {
      return Result.failure(new ScanError("Failed to walk path: " + root, e));
    }
  }

  private BeanDefinition createBeanDefinition(Class<?> clazz) {
    // Safe to assume constructors exist for loaded classes, but good to be aware
    return new BeanDefinition(clazz, clazz.getConstructors()[0].getParameterTypes());
  }

  private Result<Class<?>, ScanError> loadClass(Path root, Path file, String packageName) {
    String relative = root.relativize(file).toString();
    String className =
        packageName.concat(".").concat(relative).replace(File.separator, ".").replace(".class", "");
    try {
      return Result.success(Class.forName(className));
    } catch (Exception e) {
      return Result.failure(new ScanError("Failed to load class: " + className, e));
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
