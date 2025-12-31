package com.nathanmcunha.minispring.container;

public record BeanDefinition(Class<?> clazz, Class<?>[] dependecies) {}
