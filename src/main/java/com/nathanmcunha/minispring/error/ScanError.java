package com.nathanmcunha.minispring.error;

public record ScanError(String errorMsg, Throwable cause) implements ContextError {}
