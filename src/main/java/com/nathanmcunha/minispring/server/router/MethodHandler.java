package com.nathanmcunha.minispring.server.router;

import java.lang.reflect.Method;

public record MethodHandler(Object instance, Method method) {
}
 
