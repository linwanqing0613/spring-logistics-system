package com.example.userservice.controller;

import com.example.userservice.dto.LoginRequestDTO;
import com.example.common.dto.ResponseDTO;
import com.example.userservice.dto.UserDTO;
import com.example.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<Void>> register(@Valid @RequestBody UserDTO userDTO){
        userService.register(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseDTO.create("User registered successfully", null)
        );
    }
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<String>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO){
        String token = userService.login(loginRequestDTO);
        return ResponseEntity.ok(
                ResponseDTO.success("User logged in successfully", token)
        );
    }
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<String>> logout(HttpServletRequest request) {
        String token = extractToken(request);
        userService.logout(token);
        return ResponseEntity.ok(
                ResponseDTO.success("User logged out successfully", null)
        );
    }
    @DeleteMapping
    public ResponseEntity<ResponseDTO<Void>> deleteUserAccount(
            HttpServletRequest request,
            @Valid @RequestBody LoginRequestDTO loginRequestDTO)
    {
        String token = extractToken(request);
        userService.deleteUserAccount(loginRequestDTO, token);
        return ResponseEntity.ok(
                ResponseDTO.success("User account deleted successfully", null)
        );
    }
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header format. Expected 'Bearer <token>'.");
        }
        return authHeader.substring(7);
    }
}
