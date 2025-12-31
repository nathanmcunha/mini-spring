package com.nathanmcunha.minispring.server.router;

public record RouteError(int statusCode, String message, Throwable cause) {}
