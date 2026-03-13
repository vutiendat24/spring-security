package com.springsecurity.springsecurity.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springsecurity.springsecurity.DTO.Request.IntrospectRequest;
import com.springsecurity.springsecurity.DTO.Request.LoginRequest;
import com.springsecurity.springsecurity.DTO.Request.RegisterRequest;
import com.springsecurity.springsecurity.DTO.Response.IntrospectResponse;
import com.springsecurity.springsecurity.DTO.Response.UserDTO;
import com.springsecurity.springsecurity.Entity.User;
import com.springsecurity.springsecurity.Exception.AppException;
import com.springsecurity.springsecurity.Exception.ErrorCode;
import com.springsecurity.springsecurity.repository.UserRepository;
import com.springsecurity.springsecurity.ultil.Convert;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    UserRepository userRepository;
    JWTService jwtService;
    PasswordEncoder passwordEncoder;

    public UserDTO register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encodedPassword);
        userRepository.save(user);
        return Convert.convertUserToUserDTO(user);
    }

    public String login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_LOGIN);
        }
        String token = jwtService.generateAccessToken(user);

        System.out.println(jwtService.verifyAccessToken(token));
        return token;
    }

    public IntrospectResponse introspect(IntrospectRequest introspectRequest) {
        String token = introspectRequest.getToken();
        boolean isValid = jwtService.verifyAccessToken(token);

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }
}
