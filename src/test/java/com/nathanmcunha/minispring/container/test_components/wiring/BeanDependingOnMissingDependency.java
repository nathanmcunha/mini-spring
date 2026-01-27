package com.nathanmcunha.minispring.container.test_components.wiring;

import com.nathanmcunha.minispring.annotations.Component;

@Component
public class BeanDependingOnMissingDependency {
    public BeanDependingOnMissingDependency(NonExistentDependency dependency) {}
}
