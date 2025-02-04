package com.example.userservice.service.impl;

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
    @Override
    public void register(UserDTO userDTO) {
        log.info("Attempting to register user with username: {}", userDTO.getUsername());
        Optional<User> existingUser = userRepository.findByUsername(userDTO.getUsername());
        if (existingUser.isPresent()) {
            log.warn("Username already registered: {}", userDTO.getUsername());
            throw new BadRequestException("register: Username already registered");
        }
        User user = new User(
                uuidProvider.generateUUID(),
                userDTO.getUsername(),
                passwordEncoder.encode(userDTO.getPassword()),
                userDTO.getEmail(),
                userDTO.getPhone(),
                userDTO.getRole(),
                userDTO.getStatus());
        userRepository.save(user);
        log.info("User registered successfully with username: {}", userDTO.getUsername());
    }

    @Override
    public String login(LoginRequestDTO loginRequestDTO) {
        log.info("Attempting login for user: {}", loginRequestDTO.getUsername());
        User user = userRepository.findByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            log.warn("Invalid credentials for user: {}", loginRequestDTO.getUsername());
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().toString()
        );
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getId(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("User {} logged in successfully with token: {}", user.getUsername(), token);
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
        User user = validateUserWithToken(token, loginRequestDTO.getUsername());

        userRepository.delete(user);
        SecurityContextHolder.clearContext();
        log.info("User account deleted successfully for username: {}", loginRequestDTO.getUsername());
    }
    private User validateUserWithToken(String token, String username) {
        log.info("Validating user with token for username: {}", username);
        if (token == null || !jwtTokenProvider.isValidToken(token)) {
            log.warn("Invalid or missing token for user: {}", username);
            throw new UnauthorizedException("validate User: Invalid or missing token");
        }

        String tokenUserId = jwtTokenProvider.getUserIdFromToken(token);
        String tokenUsername = jwtTokenProvider.getUsernameFromToken(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("validate User: User not found"));

        if (!tokenUserId.equals(user.getId()) || !tokenUsername.equals(user.getUsername())) {
            log.warn("Unauthorized access attempt for user: {} with token: {}", username, token);
            throw new ForbiddenException("validate User: Unauthorized to perform this operation");
        }
        log.info("User validation successful for username: {}", username);
        return user;
    }
}
