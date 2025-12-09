package com.nathanmcunha.minispring.context.test_components.circular;

import com.nathanmcunha.minispring.annotation.Component;

@Component
public class CircularA {
  private final CircularB circularB;

  public CircularA(CircularB circularB) {
    this.circularB = circularB;
  }
}
