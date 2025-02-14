package com.example.user_service.controller;

import com.example.user_service.dto.LoginRequestDTO;
import com.example.common.dto.ResponseDTO;
import com.example.common.dto.OnRegister;
import com.example.common.dto.OnUpdate;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public ResponseEntity<ResponseDTO<User>> info(){
        User user = userService.info();
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
            @Validated(OnUpdate.class) @RequestBody UserDTO userDTO
    ){
        userService.update(userDTO);
        User user = userService.info();
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
    public ResponseEntity<ResponseDTO<String>> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token.substring(7));
        return ResponseEntity.ok(
                ResponseDTO.success("User logged out successfully", null)
        );
    }
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDTO<Void>> deleteUserAccount(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO)
    {
        userService.deleteUserAccount(loginRequestDTO);
        return ResponseEntity.ok(
                ResponseDTO.success("User account deleted successfully", null)
        );
    }
}
