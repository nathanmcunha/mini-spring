package com.nathanmcunha.minispring.common;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

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
    return switch (this) {
      case Success<T, E>(var value) -> success(mapper.apply(value));
      case Failure<T, E>(var error) -> failure(error);
    };
  }

  default <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
    return switch (this) {
      case Success<T, E>(var value) -> mapper.apply(value);
      case Failure<T, E>(var error) -> failure(error);
    };
  }

  default T orDefault(Supplier<T> other) {
    return switch (this) {
      case Success<T, E>(var value) -> value;
      case Failure<T, E> f -> other.get();
    };
  }

  default <F> Result<T, F> mapError(Function<E, F> mapper) {
    return switch (this) {
      case Failure<T, E>(var error) -> failure(mapper.apply(error));
      case Success<T, E>(var value) -> success(value);
    };
  }

  default Stream<T> stream() {
    return switch (this) {
      case Success<T, E>(var value) -> Stream.of(value);
      case Failure<T, E> f -> Stream.empty();
    };
  }

  public static <T, R, E, A, C> Result<C, E> traverse(
      Iterable<T> inputs, Function<T, Result<R, E>> mapper, Collector<R, A, C> collector) {
    A accumulator = collector.supplier().get();
    var biConsumer = collector.accumulator();
    for (T input : inputs) {
      switch (mapper.apply(input)) {
        case Failure<R, E>(var error) -> {
          return failure(error);
        }
        case Success<R, E>(var value) -> biConsumer.accept(accumulator, value);
      }
    }

    return success(collector.finisher().apply(accumulator));
  }
}
