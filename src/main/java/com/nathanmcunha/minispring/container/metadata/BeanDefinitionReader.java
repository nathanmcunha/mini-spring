package com.nathanmcunha.minispring.container.metadata;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.FrameworkError;
import java.util.Set;

/**
 * Strategy interface for parsing bean configurations and creating {@link BeanDefinition}s.
 *
 * <p>Implementations may read from various sources such as annotated classes,
 * XML files, or external properties.</p>
 */
public interface BeanDefinitionReader {

  /**
   * Scans a configuration source to register bean definitions.
   *
   * @param config The configuration class (or anchor class) to start scanning from.
   * @return A Result containing a Set of discovered {@link BeanDefinition}s, or an error if scanning fails.
   */
  Result<Set<BeanDefinition>, FrameworkError> scan(Class<?> config);
}
