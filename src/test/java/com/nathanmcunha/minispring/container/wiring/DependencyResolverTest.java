package com.nathanmcunha.minispring.container.wiring;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.container.metadata.BeanDefinition;
import com.nathanmcunha.minispring.container.test_components.circular.CircularA;
import com.nathanmcunha.minispring.container.test_components.circular.CircularB;
import com.nathanmcunha.minispring.container.test_components.simple.MyTestComponent;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DependencyResolverTest {
  @ParameterizedTest(name = "{0}")
  @MethodSource("graphProvider")
  void shouldResolveVariousGraphs(
      String description, Set<BeanDefinition> definitions, boolean shouldSucceed) {
    var result = new DependencyResolver().resolve(definitions);

    if (shouldSucceed) {
      assertTrue(
          result instanceof Result.Success,
          () ->
              "Expected success for ["
                  + description
                  + "], but failed with: "
                  + ((Result.Failure<?, ?>) result).error());
    } else {
      assertTrue(
          result instanceof Result.Failure,
          () -> "Expected failure for [" + description + "], but it succeeded unexpectedly.");
    }
  }

  static Stream<Arguments> graphProvider() {
    return Stream.of(
        arguments("Simple single bean resolution", Set.of(def(MyTestComponent.class)), true),
        arguments(
            "Circular dependency detection between two beans",
            Set.of(def(CircularA.class), def(CircularB.class)),
            false));
  }

  private static BeanDefinition def(Class<?> clazz) {
    return new BeanDefinition(clazz, clazz.getDeclaredConstructors()[0].getParameterTypes());
  }
}
