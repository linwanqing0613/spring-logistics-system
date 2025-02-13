package com.example.userservice.service;

import com.example.userservice.dto.LoginRequestDTO;
import com.example.userservice.dto.UserDTO;
import com.example.userservice.entity.User;

public interface UserService {
    public User info(String token);
    public void register(UserDTO userDTO);
    public void update(UserDTO userDTO, String token);
    public String login(LoginRequestDTO loginRequestDTO);
    public void logout(String token);
    public void deleteUserAccount(LoginRequestDTO loginRequestDTO, String token);
}