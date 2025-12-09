package com.nathanmcunha.minispring.context.test_components.di;

import com.nathanmcunha.minispring.annotation.Component;

@Component
public class MyAnotherComponentTest {
  private DependecyOfMyAnother dependecyOfMyAnother;

  public MyAnotherComponentTest(DependecyOfMyAnother dependecyOfMyAnother) {
    this.dependecyOfMyAnother = dependecyOfMyAnother;
  }

  public DependecyOfMyAnother getDependecyOfMyAnother() {
    return dependecyOfMyAnother;
  }
}
