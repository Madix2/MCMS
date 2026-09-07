package com.redcode.mcms.service;

import com.redcode.mcms.dto.LoginRequest;
import com.redcode.mcms.dto.LoginResponse;
import com.redcode.mcms.entity.Role;
import com.redcode.mcms.entity.User;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.exception.NotFoundException;
import com.redcode.mcms.repository.UserRepository;
import com.redcode.mcms.security.AuthContext;
import com.redcode.mcms.security.JwtUtil;
import com.redcode.mcms.util.PasswordHasher;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.Optional;

/**
 * Handles user authentication: verifying credentials and issuing JWTs.
 */
@Stateless
public class AuthService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private JwtUtil jwtUtil;

    @Inject
    private AuthContext authContext;

    @Inject
    private AuditService auditService;

    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("Username and password are required.");
        }

        Optional<User> maybeUser = userRepository.findByUsername(request.getUsername().trim());
        if (maybeUser.isEmpty() || !PasswordHasher.verify(request.getPassword(), maybeUser.get().getPasswordHash())) {
            throw new BusinessException("Invalid username or password.");
        }

        User user = maybeUser.get();
        if (!user.isActive()) {
            throw new BusinessException("This account has been deactivated. Contact an administrator.");
        }

        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole().name());

        auditService.log("LOGIN", "User", user.getUsername() + " logged in");

        return new LoginResponse(token, user.getUsername(), user.getRole().name(),
                user.getEmployee() != null ? user.getEmployee().getFullName() : user.getUsername());
    }

    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("Password must be at least 6 characters.");
        }
        if (!authContext.isAuthenticated()) {
            throw new BusinessException("You are not logged in.");
        }
        User user = userRepository.findByUsername(authContext.getUser().getUsername())
                .orElseThrow(() -> new NotFoundException("User not found."));
        user.setPasswordHash(PasswordHasher.hash(newPassword));
        userRepository.update(user);
        auditService.log("PASSWORD_CHANGE", "User", user.getUsername() + " changed their password");
    }
}
