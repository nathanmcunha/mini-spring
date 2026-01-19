package com.nathanmcunha.minispring.server.router;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.FrameworkError;
import com.nathanmcunha.minispring.server.router.model.MethodHandler;

/**
 * Strategy interface for resolving HTTP requests to specific method handlers.
 */
public interface Router {
  /**
   * Finds the appropriate handler for a given HTTP verb and path.
   *
   * @param verb The HTTP method (e.g., GET, POST).
   * @param path The request URI path.
   * @return A Result containing the {@link MethodHandler} if found, or a {@link FrameworkError}.
   */
  Result<MethodHandler, FrameworkError> getHandler(String verb, String path);
}