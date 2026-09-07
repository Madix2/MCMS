package com.redcode.mcms.security;

import com.redcode.mcms.exception.ForbiddenException;
import jakarta.enterprise.context.RequestScoped;

/**
 * Holds the authenticated user for the current HTTP request.
 * This is injected into services so audit logging and business rules know
 * who is performing each action, and to enforce role-based access control.
 */
@RequestScoped
public class AuthContext {

    private CurrentUser currentUser;

    public CurrentUser getUser() {
        return currentUser;
    }

    public void setUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Enforces that the current user holds at least one of the given roles.
     * ADMIN is always allowed. Throws {@link ForbiddenException} otherwise.
     */
    public void requireRole(RolePermission... allowedRoles) {
        if (!isAuthenticated()) {
            throw new ForbiddenException("You are not authorised to perform this action.");
        }
        if (currentUser.hasRole("ADMIN")) {
            return;
        }
        for (RolePermission role : allowedRoles) {
            if (currentUser.getRole().equalsIgnoreCase(role.name())) {
                return;
            }
        }
        throw new ForbiddenException("You are not authorised to perform this action.");
    }

    /** Allowed roles for authorisation checks. */
    public enum RolePermission {
        MANAGER, ADMIN, SALES, INVENTORY, PROCUREMENT, FINANCE, HR, MARKETING
    }
}
