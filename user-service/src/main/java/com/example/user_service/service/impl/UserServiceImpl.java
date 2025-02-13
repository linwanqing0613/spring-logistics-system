package com.example.userservice.service.impl;

import com.example.common.dto.ModelName;
import com.example.common.dto.UserRole;
import com.example.common.dto.UserStatus;
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
import com.example.userservice.service.UserEventPublisher;
import com.example.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private JwtBlackListService jwtBlackListService;
    @Autowired
    private UUIDProvider uuidProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserEventPublisher userEventPublisher;

    @Override
    public User info(String token) {
        String userId = validateToken(token);
        log.info("Attempting to get user with ID: {}", userId);
        User user= userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("info: User not found"));
        userEventPublisher.sendUserQueriedEvent(userId);
        return user;
    }

    @Override
    public void register(UserDTO userDTO) {
        log.info("Attempting to register user with username: {}", userDTO.getUsername());
        Optional<User> existingUser = userRepository.findByUsername(userDTO.getUsername());
        if (existingUser.isPresent()) {
            log.warn("register: Username already registered: {}", userDTO.getUsername());
            throw new BadRequestException("register: Username already registered");
        }
        User user = updateUserFromDTO(uuidProvider.generateUUID(ModelName.USER), userDTO);
        userRepository.save(user);
        userEventPublisher.sendUserRegisteredEvent(user.getId());
    }

    @Override
    public void update(UserDTO userDTO, String token) {
        log.info("Attempting to update user with username: {}", userDTO.getUsername());
        Optional<User> existingUser = userRepository.findByUsername(userDTO.getUsername());
        if (existingUser.isPresent()) {
            log.warn("update: Username already registered: {}", userDTO.getUsername());
            throw new BadRequestException("register: Username already registered");
        }
        String userId = validateToken(token);
        User user = updateUserFromDTO(userId, userDTO);
        userRepository.save(user);
        userEventPublisher.sendUserUpdatedEvent(userId);
    }

    @Override
    public String login(LoginRequestDTO loginRequestDTO) {
        log.info("Attempting login for user: {}", loginRequestDTO.getUsername());
        User user = userRepository.findByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new UnauthorizedException("login: User not found"));

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            log.warn("login: Invalid credentials for user: {}", loginRequestDTO.getUsername());
            throw new UnauthorizedException("login: Invalid credentials");
        }
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
        log.info("login: User {} logged in successfully with token: {}", user.getUsername(), token);
        return token;
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("logout: Token is required for logout");
            throw new UnauthorizedException("logout: Token is required for logout");
        }
        if (jwtBlackListService.isTokenBlackList(jwtTokenProvider.getJtiFromToken(token))) {
            log.warn("logout: Token already blacklisted. JTI: {}", jwtTokenProvider.getJtiFromToken(token));
            SecurityContextHolder.clearContext();
            return;
        }
        if (!jwtTokenProvider.isValidToken(token)) {
            log.warn("logout: Invalid token expiration - Token already expired");
            SecurityContextHolder.clearContext();
            return;
        }
        long expiration = jwtTokenProvider.getExpirationFromToken(token);
        String jti = jwtTokenProvider.getJtiFromToken(token);

        jwtBlackListService.addToBlackList(jti, expiration);
        SecurityContextHolder.clearContext();
        log.info("logout: Token added to blacklist. JTI: {}, Expiration: {} seconds", jti, expiration);
    }
    @Override
    public void deleteUserAccount(LoginRequestDTO loginRequestDTO, String token) {
        log.info("Attempting to delete user account for username: {}", loginRequestDTO.getUsername());
        String userId = validateToken(token);
        User checkUser = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("delete User: User not found"));
        User user = userRepository.findByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new UnauthorizedException("delete User: User not found"));

        if (!checkUser.getId().equals(user.getId()) || !checkUser.getUsername().equals(user.getUsername())) {
            log.warn("delete User: Unauthorized access attempt for user: {} with token: {}", loginRequestDTO.getUsername(), token);
            throw new ForbiddenException("validate User: Unauthorized to perform this operation");
        }
        userRepository.delete(user);
        SecurityContextHolder.clearContext();
        userEventPublisher.sendUserDeletedEvent(user.getId());
    }
    private String validateToken(String token) {
        log.info("Validating user with token");
        if (token == null || !jwtTokenProvider.isValidToken(token)) {
            log.warn("validate: Invalid or missing token for user: {}", token);
            throw new UnauthorizedException("validate User: Invalid or missing token");
        }
        String jti = jwtTokenProvider.getJtiFromToken(token);
        if(jwtBlackListService.isTokenBlackList(jti)){
            throw new UnauthorizedException("validate User: validate User: token is blacklisted ");
        }
        String tokenUserId = jwtTokenProvider.getUserIdFromToken(token);

        log.info("validate: Token validation successful for ID: {}", tokenUserId);
        return tokenUserId;
    }
    private User updateUserFromDTO(String userid, UserDTO userDTO) {
        User user = userRepository.findById(userid).orElse(new User(userid));

        Optional.ofNullable(userDTO.getUsername()).ifPresent(user::setUsername);
        Optional.ofNullable(userDTO.getPassword()).ifPresent(pwd -> user.setPassword(passwordEncoder.encode(pwd)));
        Optional.ofNullable(userDTO.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(userDTO.getPhone()).ifPresent(user::setPhone);

        return user;
    }
}
