package com.nathanmcunha.minispring.server.test_components.rest;

import com.nathanmcunha.minispring.annotations.Get;
import com.nathanmcunha.minispring.annotations.Rest;
import com.nathanmcunha.minispring.server.model.HttpStatus;
import com.nathanmcunha.minispring.server.model.Response;

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
