package com.nathanmcunha.minispring.container.test_components.circular;

import com.nathanmcunha.minispring.annotations.Component;

@Component
public class CircularA {
  private final CircularB circularB;

  public CircularA(CircularB circularB) {
    this.circularB = circularB;
  }
}
