package com.nathanmcunha.minispring.context.interfaces;

public record BeanDefinition(Class<?> clazz, Class<?>[] dependecies) {}
