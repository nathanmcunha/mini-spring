package com.nathanmcunha.minispring.server.router;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.FrameworkError;
import com.nathanmcunha.minispring.server.router.model.MethodHandler;

public interface Router {
  Result<MethodHandler, FrameworkError> getHandler(String verb, String path);
}