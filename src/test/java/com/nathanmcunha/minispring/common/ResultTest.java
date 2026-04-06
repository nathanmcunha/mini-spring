package com.nathanmcunha.minispring.common;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void shouldReturnSuccessValueWhenMapIsCalledOnSuccess() {
        Result<String, String> result = Result.success("hello");
        Result<Integer, String> mapped = result.map(String::length);
        
        assertInstanceOf(Result.Success.class, mapped);
        assertEquals(5, ((Result.Success<Integer, String>) mapped).value());
    }

    @Test
    void shouldReturnFailureWhenMapIsCalledOnFailure() {
        Result<String, String> result = Result.failure("error");
        Result<Integer, String> mapped = result.map(String::length);
        
        assertInstanceOf(Result.Failure.class, mapped);
        assertEquals("error", ((Result.Failure<Integer, String>) mapped).error());
    }

    @Test
    void shouldReturnSuccessValueWhenFlatMapIsCalledOnSuccess() {
        Result<String, String> result = Result.success("hello");
        Result<Integer, String> mapped = result.flatMap(s -> Result.success(s.length()));
        
        assertInstanceOf(Result.Success.class, mapped);
        assertEquals(5, ((Result.Success<Integer, String>) mapped).value());
    }

    @Test
    void shouldReturnFailureWhenFlatMapIsCalledOnFailure() {
        Result<String, String> result = Result.failure("error");
        Result<Integer, String> mapped = result.flatMap(s -> Result.success(s.length()));
        
        assertInstanceOf(Result.Failure.class, mapped);
        assertEquals("error", ((Result.Failure<Integer, String>) mapped).error());
    }

    @Test
    void shouldReturnSuccessValueWhenOrDefaultIsCalledOnSuccess() {
        Result<String, String> result = Result.success("hello");
        String value = result.orDefault(() -> "default");
        assertEquals("hello", value);
    }

    @Test
    void shouldReturnDefaultValueWhenOrDefaultIsCalledOnFailure() {
        Result<String, String> result = Result.failure("error");
        String value = result.orDefault(() -> "default");
        assertEquals("default", value);
    }

    @Test
    void shouldReturnSuccessWhenMapErrorIsCalledOnSuccess() {
        Result<String, String> result = Result.success("hello");
        Result<String, Integer> mapped = result.mapError(String::length);
        
        assertInstanceOf(Result.Success.class, mapped);
        assertEquals("hello", ((Result.Success<String, Integer>) mapped).value());
    }

    @Test
    void shouldReturnMappedErrorWhenMapErrorIsCalledOnFailure() {
        Result<String, String> result = Result.failure("error");
        Result<String, Integer> mapped = result.mapError(String::length);
        
        assertInstanceOf(Result.Failure.class, mapped);
        assertEquals(5, ((Result.Failure<String, Integer>) mapped).error());
    }

    @Test
    void shouldReturnSingleElementStreamWhenStreamIsCalledOnSuccess() {
        Result<String, String> result = Result.success("hello");
        List<String> list = result.stream().collect(Collectors.toList());
        
        assertEquals(1, list.size());
        assertEquals("hello", list.get(0));
    }

    @Test
    void shouldReturnEmptyStreamWhenStreamIsCalledOnFailure() {
        Result<String, String> result = Result.failure("error");
        Stream<String> stream = result.stream();
        
        assertEquals(0, stream.count());
    }

    @Test
    void shouldAccumulateValuesWhenTraverseIsCalledOnAllSuccesses() {
        List<Integer> inputs = List.of(1, 2, 3);
        Result<List<Integer>, String> result = Result.traverse(
            inputs,
            i -> Result.success(i * 2),
            Collectors.toList()
        );
        
        assertInstanceOf(Result.Success.class, result);
        assertEquals(List.of(2, 4, 6), ((Result.Success<List<Integer>, String>) result).value());
    }

    @Test
    void shouldReturnFirstErrorWhenTraverseEncountersFailure() {
        List<Integer> inputs = List.of(1, 2, 3);
        Result<List<Integer>, String> result = Result.traverse(
            inputs,
            i -> i == 2 ? Result.failure("failed at 2") : Result.success(i),
            Collectors.toList()
        );
        
        assertInstanceOf(Result.Failure.class, result);
        assertEquals("failed at 2", ((Result.Failure<List<Integer>, String>) result).error());
    }
}
