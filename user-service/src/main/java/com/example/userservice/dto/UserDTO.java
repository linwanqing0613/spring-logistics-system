package com.example.userservice.dto;

import com.example.common.dto.UserRole;
import com.example.common.dto.UserStatus;
import jakarta.validation.constraints.NotBlank;

public class UserDTO {
    @NotBlank(message = "Username cannot be blank")
    private String username;
    @NotBlank(message = "Password cannot be blank")
    private String password;
    private String email;
    private String phone;
    private String role = UserRole.CUSTOMER.toString();
    private String status = UserStatus.ACTIVE.toString();

    // Constructors
    public UserDTO() {}

    public UserDTO(String username, String password, String email, String phone,
                   String role, String status) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
