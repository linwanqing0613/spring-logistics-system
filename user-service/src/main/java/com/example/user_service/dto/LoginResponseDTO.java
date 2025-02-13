package com.example.user_service.dto;

import com.example.common.dto.UserRole;

public class LoginResponseDTO {
    private String username;
    private String token;  // JWT Token
    private UserRole role;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String username, String token, UserRole role) {
        this.username = username;
        this.token = token;
        this.role = role;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
