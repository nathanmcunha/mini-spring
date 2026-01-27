package com.nathanmcunha.minispring.container.test_components.wiring;

import com.nathanmcunha.minispring.annotations.Component;

public class AmbiguousDependencyTest {

  public interface CommonInterface {}

  @Component
  public static class ImplementationA implements CommonInterface {}

  @Component
  public static class ImplementationB implements CommonInterface {}

  public static class BeanDependingOnAmbiguousInterface {
    public BeanDependingOnAmbiguousInterface(CommonInterface dependency) {}
  }
}
