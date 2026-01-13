package com.nathanmcunha.minispring.container.discovery;

import com.nathanmcunha.minispring.annotations.Component;
import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.metadata.BeanDefinition;
import com.nathanmcunha.minispring.container.metadata.BeanDefinitionReader;
import com.nathanmcunha.minispring.error.FrameworkError;
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

  public Result<Set<BeanDefinition>, FrameworkError> scan(Class<?> config) {
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
      return Result.failure(new FrameworkError.ScanFailed("Failed to scan resources", e));
    }
  }

  // this is responsible for I/O operation , basically is reading the folders looking for classes
  // to be mapped/discovery
  private Result<Set<BeanDefinition>, FrameworkError> scanRoot(URL resource, String packageName) {
    try {
      Path root = Paths.get(resource.toURI());

      Result<List<Path>, FrameworkError> filesResult = safeWalk(root);
      return filesResult.map(
          paths ->
              paths.stream()
                  .flatMap(file -> loadClass(root, file, packageName).stream())
                  .filter(this::isComponent)
                  .map(this::createBeanDefinition)
                  .collect(Collectors.toSet()));
    } catch (Exception e) {
      return Result.failure(new FrameworkError.ScanFailed("Scan failed for root: " + resource, e));
    }
  }

  private Result<List<Path>, FrameworkError> safeWalk(Path root) {
    try (var stream = Files.walk(root)) {
      List<Path> files =
          stream.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".class")).toList();
      return Result.success(files);
    } catch (IOException e) {
      return Result.failure(new FrameworkError.ScanFailed("Failed to walk path: " + root, e));
    }
  }

  // For Simplify the framework now , assuming that a bean has a only one constructor
  private BeanDefinition createBeanDefinition(Class<?> clazz) {
    var constructors = clazz.getDeclaredConstructors();
    if (constructors.length == 0) {
      return new BeanDefinition(clazz, new Class<?>[0]);
    }
    return new BeanDefinition(clazz, constructors[0].getParameterTypes());
  }

  private Result<Class<?>, FrameworkError> loadClass(Path root, Path file, String packageName) {
    String relative = root.relativize(file).toString();
    String className =
        packageName.concat(".").concat(relative).replace(File.separator, ".").replace(".class", "");
    try {
      return Result.success(Class.forName(className));
    } catch (Exception e) {
      return Result.failure(new FrameworkError.ScanFailed("Failed to load class: " + className, e));
    }
  }

  private boolean isComponent(Class<?> clazz) {
    if (clazz.isInterface() || clazz.isAnnotation()) {
      return false;
    }
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
