package com.nathanmcunha.minispring.container.test_components.deep;

import com.nathanmcunha.minispring.annotations.Component;

@Component
public class ServiceA {
    private final ServiceB serviceB;
    public ServiceA(ServiceB serviceB) { this.serviceB = serviceB; }
}
