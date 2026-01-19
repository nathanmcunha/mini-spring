package com.nathanmcunha.minispring.container.test_components.deep;

import com.nathanmcunha.minispring.annotations.Component;

@Component
public class ServiceC {
    private final ServiceD serviceD;
    public ServiceC(ServiceD serviceD) { this.serviceD = serviceD; }
}
