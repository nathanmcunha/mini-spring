package com.nathanmcunha.minispring.container.test_components.deep;

import com.nathanmcunha.minispring.annotations.Component;

@Component
public class ServiceB {
    private final ServiceC serviceC;
    public ServiceB(ServiceC serviceC) { this.serviceC = serviceC; }
}
