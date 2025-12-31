package com.nathanmcunha.minispring.container;

import java.util.Set;

public interface BeanDefinitionReader {
  Set<BeanDefinition> scan(String packageName);
}
