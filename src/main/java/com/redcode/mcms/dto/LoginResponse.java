package com.redcode.mcms.dto;

/**
 * Token and identity returned after successful authentication.
 */
public class LoginResponse {

    private String token;
    private String username;
    private String role;
    private String displayName;

    public LoginResponse() { }

    public LoginResponse(String token, String username, String role, String displayName) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.displayName = displayName;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
