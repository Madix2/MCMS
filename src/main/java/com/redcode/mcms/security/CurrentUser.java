package com.redcode.mcms.security;

/**
 * Represents the currently authenticated user for the duration of a request.
 * Stored in the request-scoped bean {@link AuthContext} so that services and
 * audit logging can record who performed an action.
 */
public class CurrentUser {

    private final Long id;
    private final String username;
    private final String role;

    public CurrentUser(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }

    public boolean hasRole(String requiredRole) {
        return "ADMIN".equalsIgnoreCase(role) || requiredRole.equalsIgnoreCase(role);
    }
}
