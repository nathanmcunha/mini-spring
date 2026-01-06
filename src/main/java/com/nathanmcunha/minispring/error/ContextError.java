package com.nathanmcunha.minispring.error;

public sealed interface ContextError permits ScanError, DependencyError {
}
