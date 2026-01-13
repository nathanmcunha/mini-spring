package com.nathanmcunha.minispring.example;

import com.nathanmcunha.minispring.annotations.Get;
import com.nathanmcunha.minispring.annotations.Rest;
import com.nathanmcunha.minispring.server.dispatch.protocol.Response;

@Rest
public class HelloController {

  private final HelloService service;

  // Dependency Injection happens here
  public HelloController(HelloService service) {
    this.service = service;
  }

  @Get("/hello")
  public Response<String> hello() {
    return Response.Builder(200).body(service.sayHello("Developer"));
  }

  @Get("/ping")
  public Response<String> ping() {
    return Response.Builder(200).body("pong");
  }
}
