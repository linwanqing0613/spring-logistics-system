package com.example.user_service.service;

import com.example.common.dto.ModelName;
import com.example.common.exception.BadRequestException;
import com.example.common.exception.ForbiddenException;
import com.example.common.exception.UnauthorizedException;
import com.example.common.security.JwtTokenProvider;
import com.example.common.service.JwtBlackListService;
import com.example.common.service.UUIDProvider;
import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
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
    @MockitoBean
    private SecurityContext securityContext;
    @MockitoBean
    private Authentication authentication;
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
    private User existingUser;
    private UserDTO userDTO;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        user = new User(
                "1",
                "testuser",
                "password123",
                "test@example.com",
                "1234567",
                "ROLE_USER",
                "active"
        );
        existingUser = new User(
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
                existingUser.getUsername(),
                existingUser.getPassword()
        );
    }
    @Test
    void testInfo_Success() {
        Mockito.when(authentication.getName()).thenReturn(user.getId());
        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user));

        userService.info();
        Mockito.verify(userRepository).findById(Mockito.any(String.class));
    }
    @Test
    void testInfo_Failure_IsBlacklist() {
        Mockito.when(authentication.getName()).thenReturn(null);
        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class, () -> userService.info());
        Mockito.verify(userEventPublisher, Mockito.never()).sendUserUpdatedEvent(Mockito.any());
    }
    @Test
    void testRegister_Success() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(uuidProvider.generateUUID(ModelName.USER)).thenReturn("UUID");
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");

        userService.register(userDTO);

        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }
    @Test
    void testUpdate_Success() {
        Mockito.when(authentication.getName()).thenReturn(user.getId());

        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");


        userService.update(userDTO);
        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verify(userEventPublisher).sendUserUpdatedEvent(Mockito.anyString());
    }
    @Test
    void testUpdate_Failure_UsernameAlreadyExists() {
        Mockito.when(authentication.getName()).thenReturn(user.getId());

        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.of(existingUser));
        Mockito.when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");

        assertThrows(BadRequestException.class, () -> userService.update(userDTO));

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
    void testLogin_Failure_UserNotFound() {
        Mockito.when(userRepository.findByUsername(loginRequestDTO.getUsername())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> userService.login(loginRequestDTO));
    }
    @Test
    void testLogin_Failure_InvalidPassword() {
        Mockito.when(userRepository.findByUsername(loginRequestDTO.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(Mockito.any(), Mockito.any())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.login(loginRequestDTO));
    }
    @Test
    void testLogout_Success() {
        String token = "valid-jwt-token";

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
        Mockito.when(authentication.getName()).thenReturn(user.getId());
        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.of(user));

        userService.deleteUserAccount(loginRequestDTO);

        Mockito.verify(userRepository).delete(Mockito.any(User.class));
    }
    @Test
    void testDeleteUserAccount_Failure_Forbidden() {
        String wrongId = "wrong-user-id";
        Mockito.when(authentication.getName()).thenReturn(wrongId);
        Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(new User(wrongId)));
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class, () -> userService.deleteUserAccount(loginRequestDTO));

        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any());
        Mockito.verify(userEventPublisher, Mockito.never()).sendUserUpdatedEvent(Mockito.any());
    }
    @Test
    void testDeleteUserAccount_Failure_InvalidToken() {
        Mockito.when(authentication.getName()).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> userService.deleteUserAccount(loginRequestDTO));

        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any());
        Mockito.verify(userEventPublisher, Mockito.never()).sendUserUpdatedEvent(Mockito.any());
    }

}