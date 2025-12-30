package com.nathanmcunha.minispring.context.interfaces;

import java.util.Set;

public interface BeanDefinitionReader {
  Set<BeanDefinition> scan(String packageName);
}
