package com.nathanmcunha.minispring.server;

import java.util.HashMap;
import java.util.Map;

public record Response<T>(int statusCode, T body, Map<String, String> headers) {
  public Response {
    if (statusCode < 100) throw new IllegalArgumentException("Invalid status code");
    headers = headers == null ? Map.of() : headers;
  }

  public static interface Builder {
    Builder header(String key, String value);

    <T> Response<T> body(T body);

    <T> Response<T> build();
  }

  public static DefaultBuilder Builder(int status) {
    return new DefaultBuilder(status);
  }

  public static class DefaultBuilder implements Builder {
    private final int status;
    private final Map<String, String> headers = new HashMap<>();

    public DefaultBuilder(int status) {
      this.status = status;
    }

    public DefaultBuilder header(String key, String value) {
      this.headers.put(key, value);
      return this;
    }

    public <T> Response<T> body(T body) {
      return new Response<>(status, body, headers);
    }

    public <T> Response<T> build() {
      return new Response<>(status, null, headers);
    }
  }
}
