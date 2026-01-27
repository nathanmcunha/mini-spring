package com.nathanmcunha.minispring.server.dispatch;

import com.nathanmcunha.minispring.common.Result;
import com.nathanmcunha.minispring.error.FrameworkError;
import com.nathanmcunha.minispring.server.dispatch.protocol.HttpStatus;
import com.nathanmcunha.minispring.server.dispatch.protocol.Response;
import com.nathanmcunha.minispring.server.router.RouteAction;
import com.nathanmcunha.minispring.server.router.Router;
import com.nathanmcunha.minispring.server.router.model.MethodHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

/**
 * The primary entry point for HTTP requests, acting as the Front Controller for the framework.
 *
 * <p>The DispatcherServlet receives {@link HttpExchange} objects from the server,
 * consults the {@link Router} to find a matching handler, and coordinates the execution
 * of the associated controller method. It is also responsible for writing the
 * final response and handling any framework-level errors that occur during processing.</p>
 */
public class DispatcherServlet implements HttpHandler {

  private final Router router;

  public DispatcherServlet(final Router router) {
    this.router = router;
  }

  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    String verb = exchange.getRequestMethod();
    String path = exchange.getRequestURI().getPath();

    // Pipeline:
    // 1. Find Handler (or fail with RouteNotFound)
    // 2. Convert Handler to Action
    // 3. Perform Action
    Result<Response<?>, FrameworkError> result =
        router
            .getHandler(verb, path)
            .map(this::createExecutionAction)
            .flatMap(action -> action.perform(exchange));

    switch (result) {
      case Result.Success(var response) -> writeResponse(exchange, response);
      case Result.Failure(FrameworkError.RouteNotFound error) ->
          writeResponse(exchange, Response.Builder(HttpStatus.NOT_FOUND.value()).build());
      case Result.Failure(var error) -> handleError(exchange, error);
    }
  }

  private void writeResponse(HttpExchange exchange, Response<?> response) throws IOException {
    var bodyToSend = response.body() != null ? response.body() : "";
    byte[] responseBytes = bodyToSend.toString().getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(response.statusCode(), responseBytes.length);
    try (OutputStream responseBody = exchange.getResponseBody()) {
      responseBody.write(responseBytes);
    }
  }

  private void handleError(HttpExchange exchange, FrameworkError error) throws IOException {
    int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    String message = "Internal Server Error";
    Throwable cause = null;

    if (error instanceof FrameworkError.RequestHandlingFailed failure) {
      statusCode = failure.suggestedStatusCode();
      cause = failure.exception();
      message = "Error: " + cause;
    } else {
      message = "Error: " + error;
    }

    byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(statusCode, responseBytes.length);
    try (OutputStream responseBody = exchange.getResponseBody()) {
      responseBody.write(responseBytes);
    }
    
    if (cause != null) {
      System.err.println("[DispatcherServlet] Request processing failed.");
      System.err.println("  Error Details: " + message);
      System.err.println("  Exception Stack Trace:");
      cause.printStackTrace(System.err);
    } else {
      System.err.println("[DispatcherServlet] Request failed: " + error);
    }
  }

  private RouteAction createExecutionAction(MethodHandler handler) {
    return (exchange) -> {
      try {
        var result = handler.method().invoke(handler.instance());
        if (result instanceof Response<?> response) {
          return Result.success(response);
        }
        return Result.success(Response.Builder(HttpStatus.OK.value()).body(result));
      } catch (InvocationTargetException e) {
        Exception cause = (Exception) e.getCause();
        return Result.failure(
            new FrameworkError.RequestHandlingFailed(
                cause, HttpStatus.INTERNAL_SERVER_ERROR.value()));
      } catch (IllegalAccessException e) {
        return Result.failure(
            new FrameworkError.RequestHandlingFailed(
                e, HttpStatus.INTERNAL_SERVER_ERROR.value()));
      } catch (Exception e) {
        return Result.failure(
            new FrameworkError.RequestHandlingFailed(e, HttpStatus.INTERNAL_SERVER_ERROR.value()));
      }
    };
  }
}