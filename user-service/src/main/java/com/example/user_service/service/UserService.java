package com.example.user_service.service;

import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.entity.User;

public interface UserService {
    public User info(String token);
    public void register(UserDTO userDTO);
    public void update(UserDTO userDTO, String token);
    public String login(LoginRequestDTO loginRequestDTO);
    public void logout(String token);
    public void deleteUserAccount(LoginRequestDTO loginRequestDTO, String token);
}