package com.nathanmcunha.minispring.container.metadata;

public record BeanDefinition(Class<?> clazz, Class<?>[] dependecies) {}
