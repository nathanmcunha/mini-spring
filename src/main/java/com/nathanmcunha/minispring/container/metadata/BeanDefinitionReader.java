package com.nathanmcunha.minispring.container.metadata;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.FrameworkError;
import java.util.Set;

public interface BeanDefinitionReader {
  Result<Set<BeanDefinition>, FrameworkError> scan(Class<?> config);
}