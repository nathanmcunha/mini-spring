package com.nathanmcunha.minispring.example;

import com.nathanmcunha.minispring.annotations.Component;

@Component
public class HelloService {

  public String sayHello(String name) {
    return "Hello, " + name + "! Welcome to Mini-Spring.";
  }
}
