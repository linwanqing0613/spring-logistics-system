package com.example.user_service.controller;

import com.example.common.config.RedisConfig;
import com.example.common.exception.UnauthorizedException;
import com.example.common.service.JwtBlackListService;
import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
@Import(RedisConfig.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtBlackListService jwtBlackListService;
    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;
    @MockitoBean
    private RabbitTemplate rabbitTemplate;
    @BeforeEach
    void setUp() {
        userService.register(new UserDTO(
                "testuser",
                "password123",
                "test@example.com",
                "testphone")
        );
    }

    @Test
    void testRegister_Success() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "testuser",
                          "password": "password123",
                          "email": "test@example.com",
                          "phone": "12345678"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void testRegister_Failure() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "testuser",
                          "email": "test@example.com",
                          "phone": "12345678"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testUpdate_Success() throws Exception {
        String token = "Bearer valid-jwt-token";
        Mockito.doNothing().when(userService).update(any(UserDTO.class), Mockito.eq(token));
        Mockito.when(userService.info(token)).thenReturn(new User());
        mockMvc.perform(put("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "testuser",
                          "password": "password123",
                          "email": "test@example.com",
                          "phone": "12345678"
                        }
                        """)
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdate_Failure() throws Exception {
        String token = "invalid-jwt-token";
        Mockito.doNothing().when(userService).update(any(UserDTO.class), Mockito.eq(token));
        Mockito.when(userService.info(token)).thenReturn(new User());
        mockMvc.perform(put("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "testuser",
                          "email": "test@example.com",
                          "phone": "12345678"
                        }
                        """)
                .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void testLogin_Success() throws Exception {
        Mockito.when(userService.login(any(LoginRequestDTO.class))).thenReturn("mocked-jwt-token");
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "testuser",
                          "password": "password123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User logged in successfully"))
                .andExpect(jsonPath("$.data").value("mocked-jwt-token"));
    }
    @Test
    void testLogin_Failure_InvalidCredentials() throws Exception {
        Mockito.when(userService.login(any(LoginRequestDTO.class)))
                .thenThrow(new UnauthorizedException("User not found"));
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "wronguser",
                          "password": "wrongpassword"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testLogin_Failure_MissingFields() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "testuser"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
    @Test
    void testLogout_Success() throws Exception {
        String token = "mocked-jwt-token";
        Mockito.doNothing().when(userService).logout(token);

        mockMvc.perform(post("/api/user/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User logged out successfully"));

        Mockito.verify(userService, Mockito.timeout(1)).logout(token);
    }
    @Test
    void testLogout_Failure_InvalidToken() throws Exception {
        String token = "mocked-jwt-token";
        Mockito.doNothing().when(userService).logout(token);

        mockMvc.perform(post("/api/user/logout")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Authorization header format. Expected 'Bearer <token>'."));
    }
    @Test
    void testDeleteUserAccount_Success() throws Exception {
        String token = "mocked-jwt-token";
        Mockito.doNothing().when(userService).deleteUserAccount(any(LoginRequestDTO.class), Mockito.eq(token));

        mockMvc.perform(delete("/api/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "testuser",
                          "password": "password123"
                        }
                        """)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User account deleted successfully"));
        Mockito.verify(userService, Mockito.timeout(1)).deleteUserAccount(any(LoginRequestDTO.class), Mockito.eq(token));
    }
    @Test
    void testDeleteUserAccount_Failure_InvalidToken() throws Exception {
        String token = "mocked-jwt-token";
        Mockito.doNothing().when(userService).deleteUserAccount(any(LoginRequestDTO.class), Mockito.eq(token));
        mockMvc.perform(delete("/api/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "testuser",
                          "password": "password123"
                        }
                        """)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Authorization header format. Expected 'Bearer <token>'."));
    }
}