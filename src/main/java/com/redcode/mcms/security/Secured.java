package com.redcode.mcms.security;

import com.redcode.mcms.entity.Role;
import jakarta.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks REST resource methods that require authentication. Optionally restricts
 * access to the listed roles (role-based access control).
 *
 * Usage:
 *   @Secured
 *   @Secured(roles = {Role.MANAGER, Role.ADMIN})
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Secured {
    Role[] roles() default {};
}
