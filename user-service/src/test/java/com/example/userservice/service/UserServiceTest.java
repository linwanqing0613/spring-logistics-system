package com.example.userservice.service;

import com.example.common.exception.BadRequestException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;



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
    @MockitoBean
    private UserEventPublisher userEventPublisher;
    private User user;
    private User user_2;
    private UserDTO userDTO;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setUp() {
        user = new User(
                "1",
                "testuser",
                "password123",
                "test@example.com",
                "1234567",
                "ROLE_USER",
                "active"
        );
        user_2 = new User(
                "2",
                "testuser2",
                "password123",
                "test2@example.com",
                "12345678",
                "ROLE_USER",
                "active"
        );
        userDTO = new UserDTO(
                "testuser3",
                "password123",
                "test3@example.com",
                "123456789"
        );
        loginRequestDTO = new LoginRequestDTO(
                "testuser2",
                "password123"
        );
    }
    @Test
    void testInfo_Success() {
        String token = "valid-jwt-token";
        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user));

        Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn(user.getId());
        Mockito.when(jwtTokenProvider.getUsernameFromToken(Mockito.anyString())).thenReturn(user.getUsername());
        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(true);

        userService.info(token);
        Mockito.verify(userRepository).findById(Mockito.any(String.class));
    }
    @Test
    void testInfo_Failure_IsBlacklist() {
        String token = "invalid-jwt-token";
        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user));

        Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn(user.getId());
        Mockito.when(jwtTokenProvider.getUsernameFromToken(Mockito.anyString())).thenReturn(user.getUsername());
        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.info(token));
        Mockito.verify(jwtBlackListService, Mockito.never()).isTokenBlackList(Mockito.any());
        Mockito.verify(userEventPublisher, Mockito.never()).sendUserUpdatedEvent(Mockito.any());
    }
    @Test
    void testRegister_Success() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(uuidProvider.generateUUID()).thenReturn("UUID");
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");

        userService.register(userDTO);

        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }
    @Test
    void testUpdate_Success() {
        String token = "valid-jwt-token";
        Mockito.when(jwtTokenProvider.isValidToken(token)).thenReturn(true);
        Mockito.when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(user.getId());
        Mockito.when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.empty());
        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");


        userService.update(userDTO, token);
        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verify(userEventPublisher).sendUserUpdatedEvent(Mockito.anyString());
    }
    @Test
    void testUpdate_Failure_UsernameAlreadyExists() {
        String token = "valid-jwt-token";
        Mockito.when(jwtTokenProvider.isValidToken(token)).thenReturn(true);
        Mockito.when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(user.getId());
        Mockito.when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.of(user_2));
        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");

        assertThrows(BadRequestException.class, () -> userService.update(userDTO, Mockito.any(String.class)));
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(userEventPublisher, Mockito.never()).sendUserUpdatedEvent(Mockito.any());
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
    void testLogin_Failure_WrongPassword() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.login(loginRequestDTO));
    }

    @Test
    void testLogout_Success() {
        String token = "valid-jwt-token";

        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(jwtTokenProvider.getJtiFromToken(Mockito.anyString())).thenReturn("mocked-jti");
        Mockito.when(jwtTokenProvider.getExpirationFromToken(Mockito.anyString())).thenReturn(3600L);

        userService.logout(token);

        Mockito.verify(jwtBlackListService).addToBlackList(Mockito.anyString(), Mockito.anyLong());
    }
    @Test
    void testLogout_Failure_InvalidToken() {
        String token = "invalid-jwt-token";
        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(false);
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            userService.logout(token);
            mockedStatic.verify(SecurityContextHolder::clearContext);
        }
    }

    @Test
    void testDeleteUserAccount_Success() {
        String token = "valid-jwt-token";

        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn(user.getId());
        Mockito.when(jwtTokenProvider.getUsernameFromToken(Mockito.anyString())).thenReturn(user.getUsername());

        userService.deleteUserAccount(loginRequestDTO, token);

        Mockito.verify(userRepository).delete(Mockito.any(User.class));
    }
    @Test
    void testDeleteUserAccount_Failure_Forbidden() {
        String token = "valid-jwt-token";

        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user_2));
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn("wrong-user-id");
        Mockito.when(jwtTokenProvider.getUsernameFromToken(Mockito.anyString())).thenReturn(user.getUsername());

        assertThrows(ForbiddenException.class, () -> userService.deleteUserAccount(loginRequestDTO, token));

        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any());
        Mockito.verify(userEventPublisher, Mockito.never()).sendUserUpdatedEvent(Mockito.any());
    }
    @Test
    void testDeleteUserAccount_Failure_InvalidToken() {
        String token = "invalid-jwt-token";

        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.deleteUserAccount(loginRequestDTO, token));

        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any());
        Mockito.verify(userEventPublisher, Mockito.never()).sendUserUpdatedEvent(Mockito.any());
    }

    @Test
    void testDeleteUserAccount_Failure_UserNotFound() {
        String token = "valid-jwt-token";
        Mockito.when(jwtTokenProvider.isValidToken(Mockito.anyString())).thenReturn(true);
        Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn("mocked-id");
        Mockito.when(jwtTokenProvider.getUsernameFromToken(Mockito.anyString())).thenReturn("testuser");
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> userService.deleteUserAccount(loginRequestDTO, token));

        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any());
        Mockito.verify(userEventPublisher, Mockito.never()).sendUserUpdatedEvent(Mockito.any());
    }

}