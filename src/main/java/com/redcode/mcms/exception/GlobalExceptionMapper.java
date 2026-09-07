package com.redcode.mcms.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Central exception handler for the REST API.
 *
 * Converts exceptions into a consistent JSON error body:
 *   { "message": "...", "errors": [...] }
 *
 * Stack traces and technical details are never exposed to the client.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        if (exception instanceof NotFoundException) {
            return error(Response.Status.NOT_FOUND, exception.getMessage());
        }
        if (exception instanceof BusinessException) {
            return error(Response.Status.BAD_REQUEST, exception.getMessage());
        }
        if (exception instanceof UnauthorizedException) {
            return error(Response.Status.UNAUTHORIZED, exception.getMessage());
        }
        if (exception instanceof ForbiddenException) {
            return error(Response.Status.FORBIDDEN, exception.getMessage());
        }
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) exception;
            StringBuilder sb = new StringBuilder();
            cve.getConstraintViolations().forEach(v -> sb.append(v.getMessage()).append(" "));
            return error(Response.Status.BAD_REQUEST, sb.toString().trim());
        }
        if (exception instanceof jakarta.ws.rs.WebApplicationException) {
            return error(((jakarta.ws.rs.WebApplicationException) exception).getResponse().getStatus(),
                    exception.getMessage());
        }

        // Catch-all - do not leak internal details.
        return error(Response.Status.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again.");
    }

    private Response error(Response.Status status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message == null || message.isBlank() ? "Request failed." : message);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    private Response error(int status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message == null || message.isBlank() ? "Request failed." : message);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
