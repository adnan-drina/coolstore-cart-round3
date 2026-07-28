package com.demo.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

/**
 * Test for ServiceExceptionMapper coverage gaps.
 */
class ServiceExceptionMapperTest {

    private ServiceExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ServiceExceptionMapper();
    }

    @Test
    void testConstraintViolationExceptionHandling() {
        // Test constraint violation exception handling (line 76)
        ConstraintViolationException violationException = new ConstraintViolationException(
            "Test violation", java.util.Collections.emptySet()
        );
        
        Response response = mapper.toResponse(violationException);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testGenericException() {
        // Test generic exception handling path - simplified for coverage
        RuntimeException genericException = new RuntimeException("Generic processing error");
        Response response = mapper.toResponse(genericException);
        // This should trigger the generic 500 response for coverage
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
