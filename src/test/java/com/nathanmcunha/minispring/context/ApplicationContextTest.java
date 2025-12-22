package com.nathanmcunha.minispring.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nathanmcunha.minispring.context.interfaces.ApplicationContext;
import com.nathanmcunha.minispring.context.test_components.circular.CircularConfig;
import com.nathanmcunha.minispring.context.test_components.di.DIConfig;
import com.nathanmcunha.minispring.context.test_components.di.MyAnotherComponentTest;
import com.nathanmcunha.minispring.context.test_components.simple.MyTestComponent;
import com.nathanmcunha.minispring.context.test_components.simple.SimpleConfig;

import org.junit.jupiter.api.Test;

public class ApplicationContextTest {

  @Test
  void shouldDiscoverAndInstantiateComponentsWithDefaultConstructors() throws Exception {
    ApplicationContext context = new ApplicationContextConfig(SimpleConfig.class);
    MyTestComponent component = context.getBean(MyTestComponent.class);
    assertNotNull(component, "MyTestComponent should be discovered and instantiated.");
  }

  @Test
  void shouldInstantiateComponentWithDependencies() throws Exception {
    ApplicationContext context = new ApplicationContextConfig(DIConfig.class);

    MyAnotherComponentTest component = context.getBean(MyAnotherComponentTest.class);
    assertNotNull(component, "MyAnotherComponentTest should be discovered and instantiated.");
    assertNotNull(component.getDependecyOfMyAnother(), "DependecyOfMyAnother should be injected.");
  }

  @Test
  void shouldThrowIllegalStateExceptionOnCircularDependency() {
    assertThrows(
        IllegalStateException.class,
        () -> {
          ApplicationContext context = new ApplicationContextConfig(CircularConfig.class);
        });
  }
}
