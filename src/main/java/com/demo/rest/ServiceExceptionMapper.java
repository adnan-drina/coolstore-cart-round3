package com.demo.rest;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@ApplicationScoped
public class ServiceExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Service exception", exception);
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .type("application/problem+json")
            .entity(Map.of(
                "status", 503,
                "title", "Service Unavailable",
                "detail", exception.getMessage()
            ))
            .build();
    }
}
