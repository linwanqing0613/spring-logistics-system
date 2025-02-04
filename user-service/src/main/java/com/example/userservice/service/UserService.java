package com.example.userservice.service;

import com.example.userservice.dto.LoginRequestDTO;
import com.example.userservice.dto.UserDTO;

public interface UserService {
    public void register(UserDTO userDTO);
    public String login(LoginRequestDTO loginRequestDTO);
    public void logout(String token);
    public void deleteUserAccount(LoginRequestDTO loginRequestDTO, String token);
}