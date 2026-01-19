package com.nathanmcunha.minispring.container.test_components.circular;

import com.nathanmcunha.minispring.annotations.Component;

@Component
public class CircularB {
  private final CircularA circularA;

  public CircularB(CircularA circularA) {
    this.circularA = circularA;
  }
}
