package com.nathanmcunha.minispring.server.router.model;

import java.lang.reflect.Method;

public record MethodHandler(Object instance, Method method) {
}
 
