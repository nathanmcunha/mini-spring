package com.nathanmcunha.minispring.container;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.boot.MiniApplicationContext;
import com.nathanmcunha.minispring.container.test_components.circular.CircularConfig;
import com.nathanmcunha.minispring.container.test_components.di.DIConfig;
import com.nathanmcunha.minispring.container.test_components.di.DependecyOfMyAnother;
import com.nathanmcunha.minispring.container.test_components.di.MyAnotherComponentTest;
import com.nathanmcunha.minispring.container.test_components.simple.MyTestComponent;
import com.nathanmcunha.minispring.container.test_components.simple.SimpleConfig;
import com.nathanmcunha.minispring.error.FrameworkError;
import org.junit.jupiter.api.Test;

public class ApplicationContextTest {

  @Test
  public void shouldBootSuccessfullyWithSimpleComponents() throws Exception {
    var bootResult = MiniApplicationContext.boot(SimpleConfig.class);
    assertTrue(bootResult instanceof Result.Success, "Boot should succeed");
    if (bootResult instanceof Result.Success(var context)) {
        assertTrue(
            context.getBean(MyTestComponent.class).isPresent(),
            "Bean should be retrieved from context.");
    }
  }

  @Test
  public void shouldBootSuccessfullyWithDependencies() throws Exception {
    var bootResult = MiniApplicationContext.boot(DIConfig.class);
    assertTrue(bootResult instanceof Result.Success, "Boot should succeed");
    if (bootResult instanceof Result.Success(var context)) {
        assertTrue(
            context.getBean(MyAnotherComponentTest.class).isPresent(),
            "Bean should be retrieved from context.");

        assertTrue(
            context.getBean(DependecyOfMyAnother.class).isPresent(),
            "DependencyOfMyAnother should be injected.");
    }
  }

  @Test
  public void shouldReturnFailureWhenCircularDependencyDetected() {
    var bootResult = MiniApplicationContext.boot(CircularConfig.class);
    assertTrue(bootResult instanceof Result.Failure, "Boot should not succeed");

    var error = ((Result.Failure<?, FrameworkError>) bootResult).error();
    assertTrue(error instanceof FrameworkError.CircularDependencyDetected,
        "Expected CircularDependencyDetected error but got: " + error);
  }
}