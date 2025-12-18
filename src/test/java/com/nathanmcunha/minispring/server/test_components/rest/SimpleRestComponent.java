package com.nathanmcunha.minispring.server.test_components.rest;

import com.nathanmcunha.minispring.annotation.Get;
import com.nathanmcunha.minispring.annotation.Rest;
import com.nathanmcunha.minispring.server.HttpStatus;
import com.nathanmcunha.minispring.server.Response;

@Rest
public class SimpleRestComponent {

  @Get(value = "/getTest")
  public String simpleGet() {
    return "ALO";
  }

  @Get(value = "/getResponse")
  public Response<String> getResponse() {
    return Response.Builder(HttpStatus.OK.value()).body("TestResponse");
  }
}
