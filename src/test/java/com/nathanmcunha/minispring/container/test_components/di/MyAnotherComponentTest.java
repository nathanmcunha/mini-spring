package com.nathanmcunha.minispring.container.test_components.di;

import com.nathanmcunha.minispring.annotations.Component;

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
