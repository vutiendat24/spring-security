package com.springboot.spring_security.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.springboot.spring_security.DTO.req.RefreshTokenRequest;
import com.springboot.spring_security.DTO.res.AuthResponse;
import com.springboot.spring_security.DTO.res.UserDTO;
import com.springboot.spring_security.models.User;
import com.springboot.spring_security.services.UserService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public UserDTO createUser(@Valid @RequestBody User user){
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public AuthResponse loginUser(@RequestBody User user){
        return userService.login(user.getUserName(), user.getPassword());
    }

    /**
     * Refresh token: gửi refresh token cũ → nhận access token mới + refresh token mới
     * Endpoint này KHÔNG cần Authorization header (vì access token có thể đã hết hạn)
     */
    @PostMapping("/refresh")
    public AuthResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return userService.refresh(request.getRefreshToken());
    }

    /**
     * Logout: revoke refresh token hiện tại
     * Endpoint này KHÔNG cần Authorization header
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request,
                                         jakarta.servlet.http.HttpServletRequest httpRequest) {
        String accessToken = null;
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }
        userService.logout(request.getRefreshToken(), accessToken);
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    /**
     * Logout tất cả thiết bị: revoke tất cả refresh token của user
     * Endpoint này CẦN Authorization header (để biết user nào đang gọi)
     */
    @PostMapping("/logout-all")
    public ResponseEntity<String> logoutAll() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.logoutAll(UUID.fromString(userId));
        return ResponseEntity.ok("Đã đăng xuất tất cả thiết bị");
    }
}
