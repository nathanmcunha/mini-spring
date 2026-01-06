package com.nathanmcunha.minispring.error;

public sealed interface DependencyError extends ContextError {
  record CircularDependencyFailed(String message) implements DependencyError {}

  record InstantiationFailed(String message, Throwable cause) implements DependencyError {}

  record MissingDependency(String message) implements DependencyError {}
}
