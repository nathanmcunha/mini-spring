package com.nathanmcunha.minispring.container.metadata;

import java.util.Set;

public interface BeanDefinitionReader {
  Set<BeanDefinition> scan(String packageName);
}
