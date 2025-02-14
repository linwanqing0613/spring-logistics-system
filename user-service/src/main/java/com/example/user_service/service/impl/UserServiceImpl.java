package com.example.user_service.service.impl;

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
import com.example.user_service.service.UserEventPublisher;
import com.example.user_service.service.UserService;
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
    public User info() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user= userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("info: User not found"));
        userEventPublisher.sendUserQueriedEvent(userId);
        return user;
    }

    @Override
    public void register(UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findByUsername(userDTO.getUsername());
        if (existingUser.isPresent()) {
            log.warn("register: Username already registered: {}", userDTO.getUsername());
            throw new BadRequestException("register: Username already registered");
        }
        User user = new User(uuidProvider.generateUUID(ModelName.USER));
        updateUserFromDTO(user, userDTO);
        userRepository.save(user);
        userEventPublisher.sendUserRegisteredEvent(user.getId());
    }

    @Override
    public void update(UserDTO userDTO) {
        User existingUser = userRepository.findById(userDTO.getId()).orElseThrow(
                ()-> new BadRequestException("register: ID No Found: " + userDTO.getId()));


        if(userRepository.existsByEmail(userDTO.getEmail())){
            log.warn("update: Email already registered: {}", userDTO.getUsername());
            throw new BadRequestException("update: Email already registered");
        }
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!userId.equals(userDTO.getId())){
            log.warn("update: Unauthorized access attempt for user: {} ", userDTO.getId());
            throw new ForbiddenException("validate User: Unauthorized to perform this operation");
        }
        updateUserFromDTO(existingUser, userDTO);
        userRepository.save(existingUser);
        userEventPublisher.sendUserUpdatedEvent(userId);
    }

    @Override
    public String login(LoginRequestDTO loginRequestDTO) {
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
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getId(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("login: User {} logged in successfully with token: {}", user.getUsername(), token);
        return token;
    }

    @Override
    public void logout(String token) {
        long expiration = jwtTokenProvider.getExpirationFromToken(token);
        String jti = jwtTokenProvider.getJtiFromToken(token);

        jwtBlackListService.addToBlackList(jti, expiration);
        SecurityContextHolder.clearContext();
        log.info("logout: Token added to blacklist. JTI: {}, Expiration: {} seconds", jti, expiration);
    }
    @Override
    public void deleteUserAccount(LoginRequestDTO loginRequestDTO) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new UnauthorizedException("delete User: User not found"));

        if (!userId.equals(user.getId())) {
            log.warn("delete User: Unauthorized access attempt for user: {} ", loginRequestDTO.getUsername());
            throw new ForbiddenException("validate User: Unauthorized to perform this operation");
        }
        userRepository.delete(user);
        SecurityContextHolder.clearContext();
        userEventPublisher.sendUserDeletedEvent(user.getId());
    }
    private void updateUserFromDTO(User user, UserDTO userDTO) {

        Optional.ofNullable(userDTO.getUsername()).ifPresent(user::setUsername);
        Optional.ofNullable(userDTO.getPassword()).ifPresent(pwd -> user.setPassword(passwordEncoder.encode(pwd)));
        Optional.ofNullable(userDTO.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(userDTO.getPhone()).ifPresent(user::setPhone);

    }
}
