package com.nathanmcunha.minispring.server.test_components.rest;

import com.nathanmcunha.minispring.annotation.Get;
import com.nathanmcunha.minispring.annotation.Post;
import com.nathanmcunha.minispring.annotation.Rest;

@Rest
public class SimpleRestComponent {

  @Get(value = "/getTest")
  public String simpleGet() {
    return "ALO";
  }

  @Post(value = "/postTest")
  public String simplePost() {
    return "Simple Post";
  }
}
