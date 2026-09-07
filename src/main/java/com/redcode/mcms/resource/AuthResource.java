package com.redcode.mcms.resource;

import com.redcode.mcms.dto.LoginRequest;
import com.redcode.mcms.dto.LoginResponse;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Authentication endpoints. Login is public; password change requires auth.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthService authService;

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Response.ok(response).build();
    }

    @Secured
    @POST
    @Path("/change-password")
    public Response changePassword(java.util.Map<String, String> body) {
        String newPassword = body != null ? body.get("newPassword") : null;
        authService.changePassword(newPassword);
        return Response.ok(java.util.Map.of("message", "Password updated.")).build();
    }
}
