package com.example.user_service.controller;

import com.example.common.exception.UnauthorizedException;
import com.example.user_service.dto.LoginRequestDTO;
import com.example.common.dto.ResponseDTO;
import com.example.user_service.dto.OnRegister;
import com.example.user_service.dto.OnUpdate;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public ResponseEntity<ResponseDTO<User>> info(HttpServletRequest request){
        String token = extractToken(request);
        User user = userService.info(token);
        return ResponseEntity.status(HttpStatus.OK.value()).body(
                ResponseDTO.success("User get info successfully", user)
        );
    }
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<Void>> register(@Validated(OnRegister.class) @RequestBody UserDTO userDTO){
        userService.register(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(
                ResponseDTO.create("User registered successfully", null)
        );
    }
    @PutMapping("/update")
    public ResponseEntity<ResponseDTO<User>> update(
            HttpServletRequest request,
            @Validated(OnUpdate.class) @RequestBody UserDTO userDTO
    ){
        String token = extractToken(request);
        userService.update(userDTO, token);
        User user = userService.info(token);
        return ResponseEntity.status(HttpStatus.OK.value()).body(
                ResponseDTO.create("User registered successfully", user)
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
    @DeleteMapping("/delete")
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
            throw new UnauthorizedException("Invalid Authorization header format. Expected 'Bearer <token>'.");
        }
        return authHeader.substring(7);
    }
}
