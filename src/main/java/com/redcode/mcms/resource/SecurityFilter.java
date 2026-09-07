package com.redcode.mcms.resource;

import com.redcode.mcms.entity.User;
import com.redcode.mcms.exception.UnauthorizedException;
import com.redcode.mcms.repository.UserRepository;
import com.redcode.mcms.security.AuthContext;
import com.redcode.mcms.security.CurrentUser;
import com.redcode.mcms.security.JwtUtil;
import com.redcode.mcms.security.Secured;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * Authentication filter.
 *
 * Because {@link Secured} is a {@code @NameBinding}, this filter only runs for
 * resource methods annotated with {@code @Secured} (or whose class is).
 * This keeps the filter portable across application servers (RESTEasy, Jersey,
 * Apache CXF).
 *
 * Responsibilities:
 *  - Validate the {@code Authorization: Bearer <token>} JWT.
 *  - Reload the user, verify the account is active.
 *  - Populate {@link AuthContext} so services and audit logging know the user.
 *
 * Fine-grained role-based access control is additionally enforced in the
 * service layer via {@link com.redcode.mcms.security.AuthContext} and a
 * {@code requireRole(...)} helper, producing clear "not authorised" messages.
 */
@Provider
@Secured
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter {

    @Inject
    private JwtUtil jwtUtil;

    @Inject
    private UserRepository userRepository;

    @Inject
    private AuthContext authContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("You must be logged in to access this resource.");
        }

        String token = authHeader.substring("Bearer ".length());
        Map<String, String> claims = jwtUtil.validateToken(token);
        if (claims == null) {
            throw new UnauthorizedException("Your session is invalid or has expired. Please log in again.");
        }

        // Reload user to verify the account is still active and roles are current.
        User user = userRepository.findByUsername(claims.get("user"))
                .orElseThrow(() -> new UnauthorizedException("User account could not be found."));

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account has been deactivated. Contact an administrator.");
        }

        authContext.setUser(new CurrentUser(user.getId(), user.getUsername(), user.getRole().name()));
    }
}
