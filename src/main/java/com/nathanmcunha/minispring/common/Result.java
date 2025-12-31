package com.nathanmcunha.minispring.common;
import java.util.function.Function;

public sealed interface Result<T, E> {
  record Success<T, E>(T value) implements Result<T, E> {}

  record Failure<T, E>(E error) implements Result<T, E> {}

  static <T, E> Result<T, E> success(T value) {
    return new Success<>(value);
  }

  static <T, E> Result<T, E> failure(E error) {
    return new Failure<>(error);
  }

  default <U> Result<U, E> map(Function<T, U> mapper) {
    return this instanceof Success<T, E> s ? success(mapper.apply(s.value())) : (Result<U, E>) this;
  }

  default <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
    return this instanceof Success<T, E> s ? mapper.apply(s.value()) : (Result<U, E>) this;
  }
}
