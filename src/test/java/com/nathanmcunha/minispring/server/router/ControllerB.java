package com.nathanmcunha.minispring.server.router;

import com.nathanmcunha.minispring.annotations.Get;
import com.nathanmcunha.minispring.annotations.Rest;

@Rest
public class ControllerB {
  @Get("/conflict")
  public void handle() {}
}
