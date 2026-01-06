package com.nathanmcunha.minispring.container.metadata;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.ScanError;
import java.util.Set;

public interface BeanDefinitionReader {
  Result<Set<BeanDefinition>, ScanError> scan(Class<?> config);
}
