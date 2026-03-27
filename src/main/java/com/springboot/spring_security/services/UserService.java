package com.springboot.spring_security.services;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.spring_security.DTO.res.AuthResponse;
import com.springboot.spring_security.DTO.res.UserDTO;
import com.springboot.spring_security.models.RefreshToken;
import com.springboot.spring_security.models.User;
import com.springboot.spring_security.repositories.UserRepository;
import com.springboot.spring_security.ultils.Mapper;

import java.util.List;
import java.util.ArrayList;
import com.springboot.spring_security.repositories.RoleRepository;
import com.springboot.spring_security.models.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .accessTokenExpiresIn(JWTService.ACCESS_TOKEN_EXPIRY_SECONDS)
                .refreshTokenExpiresIn(refreshTokenService.getRefreshTokenExpirySeconds())
                .build();
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenStr) {
        RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(refreshTokenStr);
        User user = oldRefreshToken.getUser();

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(oldRefreshToken);

        String accessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .accessTokenExpiresIn(JWTService.ACCESS_TOKEN_EXPIRY_SECONDS)
                .refreshTokenExpiresIn(refreshTokenService.getRefreshTokenExpirySeconds())
                .build();
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenService.revokeToken(refreshTokenStr);
    }

    @Transactional
    public void logoutAll(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        refreshTokenService.revokeAllTokensByUser(user);
    }

    public UserDTO createUser(User user) {
        if (userRepository.findByUserName(user.getUserName()) != null) {
            throw new RuntimeException("User already exists");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.findByPhone(user.getPhone()) != null) {
            throw new RuntimeException("Phone already exists");
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        List<Role> defaultRoles = new ArrayList<>();
        defaultRoles
                .add(roleRepository.findById("USER").orElseThrow(() -> new RuntimeException("Role not found: USER")));
        user.setRoles(defaultRoles);

        return Mapper.toUserDTO(userRepository.save(user));

    }

    public UserDTO getUserById(UUID id) {
        return userRepository.findById(id).map(user -> {
            UserDTO userDTO = new UserDTO();
            userDTO.setUserName(user.getUserName());
            userDTO.setFullName(user.getFullName());
            userDTO.setPhone(user.getPhone());
            userDTO.setSex(user.getSex());
            return userDTO;
        }).orElse(null);
    }

}

