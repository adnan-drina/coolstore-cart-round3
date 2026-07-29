package com.demo.rest;

import java.util.Map;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String detail = exception.getConstraintViolations().iterator().next().getMessage();
        return Response.status(Response.Status.BAD_REQUEST)
            .type("application/problem+json")
            .entity(Map.of(
                "status", 400,
                "title", "Bad Request",
                "detail", detail
            ))
            .build();
    }
}