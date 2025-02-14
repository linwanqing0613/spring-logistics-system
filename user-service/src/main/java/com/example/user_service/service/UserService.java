package com.example.user_service.service;

import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.entity.User;

public interface UserService {
    public User info();
    public void register(UserDTO userDTO);
    public void update(UserDTO userDTO);
    public String login(LoginRequestDTO loginRequestDTO);
    public void logout(String token);
    public void deleteUserAccount(LoginRequestDTO loginRequestDTO);
}