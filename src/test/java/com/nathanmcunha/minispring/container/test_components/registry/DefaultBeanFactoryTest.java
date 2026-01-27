package com.nathanmcunha.minispring.container.test_components.registry;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.registry.DefaultBeanFactory;
import com.nathanmcunha.minispring.container.test_components.simple.MyTestComponent;
import com.nathanmcunha.minispring.error.FrameworkError;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

@DisplayName("DefaultBeanFactory tests")
public class DefaultBeanFactoryTest {

  private DefaultBeanFactory factory;

  @BeforeEach
  void setup() {
    factory = new DefaultBeanFactory();
  }

  @DisplayName("Should fail when registering duplicate bean")
  void shouldFailWhenRegisteringDuplicateBean() {
    var bean = new MyTestComponent();
    Map<Class<?>, Object> beans = Map.of(MyTestComponent.class, bean);

    var result1 = factory.registerBeans(beans);
    assertTrue(result1 instanceof Result.Success, "First registration should succeed");

    var result2 = factory.registerBeans(beans);
    assertTrue(result2 instanceof Result.Failure, "Second registration should fail");
    var error = ((Result.Failure<?, FrameworkError>) result2).error();
    assertTrue(error instanceof FrameworkError.InvalidRouteDefinition);
  }
}
