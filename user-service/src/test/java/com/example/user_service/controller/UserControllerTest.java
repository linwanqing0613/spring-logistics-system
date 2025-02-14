package com.example.user_service.controller;

import com.example.common.exception.ForbiddenException;
import com.example.common.exception.UnauthorizedException;
import com.example.common.security.JwtTokenProvider;
import com.example.common.service.JwtBlackListService;
import com.example.common.service.UUIDProvider;
import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private JwtBlackListService jwtBlackListService;
    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;
    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void testRegister_Success() throws Exception {
        Mockito.doNothing().when(userService).register(Mockito.any(UserDTO.class));
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
        Mockito.verify(userService).register(Mockito.any(UserDTO.class));
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
        Mockito.verify(userService, Mockito.never()).register(Mockito.any(UserDTO.class));
    }
    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void testUpdate_Success() throws Exception {
        String token = "Bearer valid-jwt-token";

        Mockito.doNothing().when(userService).update(any(UserDTO.class));
        Mockito.when(userService.info()).thenReturn(new User());

        mockMvc.perform(put("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "id": "1",
                          "username": "testuser",
                          "password": "password123",
                          "email": "test@example.com",
                          "phone": "12345678"
                        }
                        """)
                        .header("Authorization", token))
                .andExpect(status().isOk());
        Mockito.verify(userService).update(Mockito.any(UserDTO.class));
    }

    @Test
    @WithMockUser(username = "testId", roles = "CUSTOMER")
    void testUpdate_Failure_InvalidDTO() throws Exception {
        String token = "invalid-jwt-token";

        Mockito.doNothing().when(userService).update(any(UserDTO.class));
        Mockito.when(userService.info()).thenReturn(new User());
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
                .andExpect(status().isBadRequest());
        Mockito.verify(userService, Mockito.never()).update(Mockito.any(UserDTO.class));
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
    @WithMockUser(username = "testId", roles = "CUSTOMER")
    void testLogout_Success() throws Exception {
        String token = "Bearer mocked-jwt-token";
        Mockito.doNothing().when(userService).logout(token);

        mockMvc.perform(post("/api/user/logout")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User logged out successfully"));

        Mockito.verify(userService, Mockito.timeout(1)).logout(token);
    }
    @Test
    @WithMockUser(username = "testId", roles = "CUSTOMER")
    void testDeleteUserAccount_Success() throws Exception {
        String token = "mocked-jwt-token";
        Mockito.doNothing().when(userService).deleteUserAccount(any(LoginRequestDTO.class));

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
        Mockito.verify(userService, Mockito.timeout(1)).deleteUserAccount(any(LoginRequestDTO.class));
    }
    @Test
    void testDeleteUserAccount_Failure_InvalidToken() throws Exception {
        String token = "mocked-jwt-token";
        Mockito.doThrow(new ForbiddenException("validate User: Unauthorized to perform this operation"))
                .when(userService).deleteUserAccount(any(LoginRequestDTO.class));
        mockMvc.perform(delete("/api/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "testuser",
                          "password": "password123"
                        }
                        """)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }
}