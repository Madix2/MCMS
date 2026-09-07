package com.redcode.mcms.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Root JAX-RS application configuration.
 *
 * All REST resources live under the base path {@code /api}.
 *
 * NOTE: this class does <strong>not</strong> override {@link #getClasses()}.
 * Returning an explicit set from {@code getClasses()} would disable Jersey's
 * automatic classpath scanning, which is what registers every {@code @Path}
 * resource and {@code @Provider} (SecurityFilter, GlobalExceptionMapper,
 * JacksonObjectMapperProvider) in the application.
 */
@ApplicationPath("/api")
public class McmsApplication extends Application {

}
