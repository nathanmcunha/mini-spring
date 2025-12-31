package com.nathanmcunha.minispring.server.router;

import com.nathanmcunha.minispring.server.router.MethodHandler;
import java.util.Optional;

public interface HandlerMapping {

  Optional<MethodHandler> getHandler(String verb, String path);
}
