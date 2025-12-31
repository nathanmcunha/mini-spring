package com.nathanmcunha.minispring.container;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nathanmcunha.minispring.container.test_components.circular.CircularConfig;
import com.nathanmcunha.minispring.container.test_components.di.DIConfig;
import com.nathanmcunha.minispring.container.test_components.di.MyAnotherComponentTest;
import com.nathanmcunha.minispring.container.test_components.simple.MyTestComponent;
import com.nathanmcunha.minispring.container.test_components.simple.SimpleConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class ApplicationContextTest {

  @Test
  void shouldDiscoverAndInstantiateComponentsWithDefaultConstructors() throws Exception {
    ApplicationContext context = new MiniApplicationContext(SimpleConfig.class);
    Optional<MyTestComponent> component = context.getBean(MyTestComponent.class);
    assertTrue(component.isPresent(), "MyTestComponent should be discovered and instantiated.");
  }

  @Test
  void shouldInstantiateComponentWithDependencies() throws Exception {
    ApplicationContext context = new MiniApplicationContext(DIConfig.class);

    Optional<MyAnotherComponentTest> componentOpt = context.getBean(MyAnotherComponentTest.class);
    assertTrue(componentOpt.isPresent(), "MyAnotherComponentTest should be discovered and instantiated.");
    MyAnotherComponentTest component = componentOpt.get();
    assertNotNull(component.getDependecyOfMyAnother(), "DependecyOfMyAnother should be injected.");
  }

  @Test
  void shouldThrowIllegalStateExceptionOnCircularDependency() {
    assertThrows(
        IllegalStateException.class,
        () -> {
          ApplicationContext context = new MiniApplicationContext(CircularConfig.class);
        });
  }
}
