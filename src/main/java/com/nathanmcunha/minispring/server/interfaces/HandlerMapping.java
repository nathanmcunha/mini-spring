package com.nathanmcunha.minispring.server.interfaces;

import com.nathanmcunha.minispring.server.MethodHandler;
import java.util.Optional;

public interface HandlerMapping {

  Optional<MethodHandler> getHandler(String verb, String path);
}
