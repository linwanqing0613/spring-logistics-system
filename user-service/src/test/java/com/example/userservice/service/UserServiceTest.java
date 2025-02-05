package com.example.userservice.service;

import com.example.common.exception.ForbiddenException;
import com.example.common.exception.UnauthorizedException;
import com.example.common.security.JwtTokenProvider;
import com.example.common.service.JwtBlackListService;
import com.example.common.service.UUIDProvider;
import com.example.userservice.dto.LoginRequestDTO;
import com.example.userservice.dto.UserDTO;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;


@ActiveProfiles("test")
@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private JwtBlackListService jwtBlackListService;
    @MockitoBean
    private UUIDProvider uuidProvider;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    private User user;
    private UserDTO userDTO;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setUp() {
        user = new User(
                "1",
                "testuser2",
                "password123",
                "test2@example.com",
                "123456789",
                "ROLE_USER",
                "active"
        );
        userDTO = new UserDTO(
                "testuser",
                "password123",
                "test@example.com",
                "12345678",
                "ROLE_USER",
                "active"
        );
        loginRequestDTO = new LoginRequestDTO(
                "testuser2",
                "password123"
        );
    }
    @Test
    void testRegister_Success() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(uuidProvider.generateUUID()).thenReturn("UUID");
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");

        userService.register(userDTO);

        Mockito.verify(userRepository, times(1)).save(Mockito.any(User.class));
    }

    @Test
    void testLogin_Success() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(jwtTokenProvider.generateToken(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn("mocked-jwt-token");

        String token = userService.login(loginRequestDTO);

        assertEquals("mocked-jwt-token", token);
    }

    @Test
    void testLogout_Success() {
        String token = "valid-jwt-token";

        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(jwtTokenProvider.getJtiFromToken(Mockito.anyString())).thenReturn("mocked-jti");
        Mockito.when(jwtTokenProvider.getExpirationFromToken(Mockito.anyString())).thenReturn(3600L);

        userService.logout(token);

        Mockito.verify(jwtBlackListService, times(1)).addToBlackList(Mockito.anyString(), Mockito.anyLong());
    }
    @Test
    void testLogout_Failure_InvalidToken() {
        String token = "invalid-jwt-token";
        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(false);
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            userService.logout(token);
            mockedStatic.verify(SecurityContextHolder::clearContext, times(1));
        }
    }

    @Test
    void testDeleteUserAccount_Success() {
        String token = "valid-jwt-token";

        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn(user.getId());
        Mockito.when(jwtTokenProvider.getUsernameFromToken(Mockito.anyString())).thenReturn(user.getUsername());

        userService.deleteUserAccount(loginRequestDTO, token);

        Mockito.verify(userRepository, times(1)).delete(Mockito.any(User.class));
    }
    @Test
    void testDeleteUserAccount_Failure_forbidden() {
        String token = "valid-jwt-token";

        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn("wrong-user-id");
        Mockito.when(jwtTokenProvider.getUsernameFromToken(Mockito.anyString())).thenReturn(user.getUsername());

        assertThrows(ForbiddenException.class, () -> userService.deleteUserAccount(loginRequestDTO, token));
    }
    @Test
    void testDeleteUserAccount_Failure_InvalidToken() {
        String token = "invalid-jwt-token";

        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.deleteUserAccount(loginRequestDTO, token));
    }

    @Test
    void testDeleteUserAccount_Failure_UserNotFound() {
        String token = "valid-jwt-token";
        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn("mocked-id");
        Mockito.when(jwtTokenProvider.getUsernameFromToken(Mockito.anyString())).thenReturn("testuser");
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> userService.deleteUserAccount(loginRequestDTO, token));
    }

}