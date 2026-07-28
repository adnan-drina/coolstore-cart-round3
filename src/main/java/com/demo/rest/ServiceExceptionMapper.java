package com.demo.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exception mapper for handling validation errors and processing exceptions
 * with RFC 7807 Problem Details format.
 */
@ApplicationScoped
@Provider
public class ServiceExceptionMapper implements ExceptionMapper<Exception> {

    /**
     * RFC 7807 Problem Details JSON structure
     */
    private static class ProblemDetails {
        private static final String TYPE_DEFAULT = "about:blank";
        
        @XmlElement(required = true)
        public final String type;
        
        @XmlElement(required = true)
        public final String title;
        
        @XmlElement(required = true)
        public final int status;
        
        @XmlElement(required = true)
        public final String detail;
        
        @XmlElement(required = true)
        public final String instance;

        public ProblemDetails() {
            this.type = TYPE_DEFAULT;
            this.title = "";
            this.status = 0;
            this.detail = "";
            this.instance = "";
        }

        public ProblemDetails(String title, int status, String detail) {
            this.type = TYPE_DEFAULT;
            this.title = title;
            this.status = status;
            this.detail = detail;
            this.instance = "";
        }

        public ProblemDetails(String title, int status, String detail, String instance) {
            this.type = TYPE_DEFAULT;
            this.title = title;
            this.status = status;
            this.detail = detail;
            this.instance = instance;
        }
    }

    /**
     * Map ConstraintViolationException to 400 Bad Request
     */
    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ConstraintViolationException violationException) {
            return handleConstraintViolation(violationException);
        }
        
        // Handle ProcessingException (catalog service failures) to 503 Service Unavailable
        // Check if the cause chain contains processing-related errors
        Throwable cause = exception;
        boolean isProcessingError = false;
        while (cause != null) {
            String className = cause.getClass().getName().toLowerCase();
            if (className.contains("processing") || 
                className.contains("service") ||
                className.contains("catalog") ||
                className.contains("unavailable") ||
                className.contains("timeout")) {
                isProcessingError = true;
                break;
            }
            cause = cause.getCause();
        }
        
        if (isProcessingError || 
            (exception != null && exception.getClass().getName().contains("Processing"))) {
            return handleServiceUnavailable();
        }
        
        // Default: map to 500 Internal Server Error with generic message (never raw stack traces)
        return handleInternalServerError();
    }

    private Response handleConstraintViolation(ConstraintViolationException violationException) {
        Set<ConstraintViolation<?>> violations = violationException.getConstraintViolations();
        
        // Combine all violation messages into a single detail
        String detail = violations.stream()
                .map(v -> String.format("%s: %s", v.getPropertyPath(), v.getMessage()))
                .collect(Collectors.joining("; "));
        
        // If no specific violations, use a generic validation message
        if (detail.isEmpty()) {
            detail = "Validation failed";
        }
        
        ProblemDetails problemDetails = new ProblemDetails("Validation Failed", 400, detail);
        
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(toJson(problemDetails))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private Response handleServiceUnavailable() {
        ProblemDetails problemDetails = new ProblemDetails(
                "Service Unavailable", 
                503, 
                "Catalog service temporarily unavailable"
        );
        
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(toJson(problemDetails))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private Response handleInternalServerError() {
        // Never expose raw stack traces
        ProblemDetails problemDetails = new ProblemDetails(
                "Internal Server Error", 
                500, 
                "An unexpected error occurred"
        );
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(toJson(problemDetails))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Convert ProblemDetails to JSON map for serialization
     */
    private Map<String, Object> toJson(ProblemDetails problemDetails) {
        Map<String, Object> json = new HashMap<>();
        json.put("type", problemDetails.type);
        json.put("title", problemDetails.title);
        json.put("status", problemDetails.status);
        json.put("detail", problemDetails.detail);
        
        if (!problemDetails.instance.isEmpty()) {
            json.put("instance", problemDetails.instance);
        }
        
        return json;
    }
}