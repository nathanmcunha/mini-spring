package com.nathanmcunha.minispring.server.dispatch.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void shouldThrowExceptionWhenStatusCodeIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Response.Builder(99).build());
    }

    @Test
    void shouldBuildResponseWithValidStatusCode() {
        Response<?> response = Response.Builder(200).build();
        assertEquals(200, response.statusCode());
        assertNull(response.body());
        assertTrue(response.headers().isEmpty());
    }

    @Test
    void shouldAddHeaders() {
        Response<?> response = Response.Builder(200)
                .header("Content-Type", "application/json")
                .header("X-Custom-Header", "value")
                .build();
        
        assertEquals("application/json", response.headers().get("Content-Type"));
        assertEquals("value", response.headers().get("X-Custom-Header"));
    }

    @Test
    void shouldSetBody() {
        Response<String> response = Response.Builder(201)
                .<String>body("Hello World");
        
        assertEquals(201, response.statusCode());
        assertEquals("Hello World", response.body());
    }

    @Test
    void shouldInitializeWithEmptyHeadersWhenNullPassedToConstructor() {
        // Direct constructor usage to test the null check
        Response<String> response = new Response<>(200, "body", null);
        assertNotNull(response.headers());
        assertTrue(response.headers().isEmpty());
    }
}
