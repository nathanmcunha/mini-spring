package com.nathanmcunha.minispring.error;

public sealed interface FrameworkError {

  // Scan Errors
  record ScanFailed(String message, Throwable cause) implements FrameworkError {}

  // Dependency Errors
  record CircularDependencyDetected(String message) implements FrameworkError {}

  record BeanInstantiationFailed(String message, Throwable cause) implements FrameworkError {}

  record MissingDependency(String message) implements FrameworkError {}

  record AmbiguousDependency(String message) implements FrameworkError {}

  // Router Errors
  record RouteCollision(String verb, String path, String existingHandler, String newHandler)
      implements FrameworkError {}

  record RouteNotFound(String verb, String path) implements FrameworkError {}

  record ControllerBeanNotFound(Class<?> beanClass) implements FrameworkError {}

  record InvalidRouteDefinition(String message) implements FrameworkError {}

  // Server Errors
  record RequestHandlingFailed(Exception exception, int suggestedStatusCode)
      implements FrameworkError {}
}
