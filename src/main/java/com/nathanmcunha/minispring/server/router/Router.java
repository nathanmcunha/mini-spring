package com.nathanmcunha.minispring.server.router;

import java.util.Optional;
import com.nathanmcunha.minispring.server.router.model.MethodHandler;

public interface Router {

  Optional<MethodHandler> getHandler(String verb, String path);
}
