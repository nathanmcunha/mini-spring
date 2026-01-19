package com.nathanmcunha.minispring.container.registry;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.FrameworkError;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The central registry and provider for bean instances within the container.
 *
 * <p>A BeanFactory maintains a mapping between classes and their instantiated
 * singleton instances, allowing for type-safe retrieval and annotation-based discovery.</p>
 */
public interface BeanFactory {

  /**
   * Registers a collection of pre-instantiated beans into the factory.
   *
   * @param beans A map where keys are the bean classes and values are the instances.
   * @return A Result containing this factory, or an error if registration fails (e.g., duplicates).
   */
  Result<BeanFactory, FrameworkError> registerBeans(Map<Class<?>, Object> beans);

  /**
   * Retrieves a bean instance of the specified type.
   *
   * @param <T> The type of the bean.
   * @param clazz The class of the bean to retrieve.
   * @return An {@link Optional} containing the bean instance if found, or empty otherwise.
   */
  <T> Optional<T> getBean(Class<T> clazz);

  /**
   * Checks if a bean of the specified type is registered in this factory.
   *
   * @param clazz The class of the bean to check.
   * @return true if the bean is present, false otherwise.
   */
  boolean containsBean(Class<?> clazz);

  /**
   * Returns a list of all bean classes registered in this factory that are
   * annotated with the specified annotation type.
   *
   * @param annotationType The annotation class to search for.
   * @return A list of classes matching the annotation.
   */
  List<Class<?>> getBeansWithAnnotation(Class<? extends Annotation> annotationType);
}
