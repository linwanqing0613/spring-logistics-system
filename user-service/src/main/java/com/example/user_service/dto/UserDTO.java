package com.example.user_service.dto;

import com.example.common.dto.OnCreate;
import com.example.common.dto.OnRegister;
import com.example.common.dto.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public class UserDTO {
    @NotNull(message = "ID cannot be null", groups = OnUpdate.class)
    @Null(message = "ID must be null for create requests", groups = {OnCreate.class, OnRegister.class})
    private String id;
    @NotBlank(message = "Username cannot be blank", groups = OnRegister.class)
    private String username;
    @NotBlank(message = "Password cannot be blank", groups = OnRegister.class)
    private String password;
    private String email;
    private String phone;

    public UserDTO() {}

    public UserDTO(String username, String password, String email, String phone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
