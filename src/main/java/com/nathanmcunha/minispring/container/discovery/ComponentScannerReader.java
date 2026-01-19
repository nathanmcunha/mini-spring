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

/**
 * Scans the classpath for classes that should be managed by the IoC container.
 *
 * <p>The scanning process starts from the package of the provided configuration class.
 * Only classes within this package or its sub-packages are considered for discovery.
 * To scan the entire project, the configuration class should be placed in the project's root package.</p>
 */
public class ComponentScannerReader implements BeanDefinitionReader {

  /**
   * Scans for components starting from the package of the specified configuration class.
   *
   * @param config The configuration class used as a base for scanning.
   * @return A Result containing the set of discovered {@link BeanDefinition}s.
   */
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

  /**
   * Recursively scans a specific classpath root for potential bean components.
   *
   * @param resource The URL representing the package directory in the classpath.
   * @param packageName The base package name corresponding to this resource.
   * @return A Result containing a set of discovered {@link BeanDefinition}s.
   */
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

  /**
   * Creates a {@link BeanDefinition} for a given class.
   *
   * <p>Currently, the implementation assumes the class has a single constructor
   * or uses the first declared constructor for dependency injection resolution.</p>
   *
   * @param clazz The class to create a definition for.
   * @return A new {@link BeanDefinition} containing constructor metadata.
   */
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
