package com.springboot.spring_security.services;

import com.springboot.spring_security.models.RefreshToken;
import com.springboot.spring_security.models.User;
import com.springboot.spring_security.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // Refresh token sống 7 ngày
    @Value("${jwt.refresh_token_expiry_days:7}")
    private int refreshTokenExpiryDays;

    // Giới hạn số phiên đồng thời tối đa
    @Value("${jwt.max_sessions:5}")
    private int maxSessions;

    public RefreshToken createRefreshToken(User user) {
        // Kiểm tra số phiên hiện tại, nếu vượt giới hạn thì revoke hết
        long activeSessions = refreshTokenRepository.countByUserAndRevokedFalse(user);
        if (activeSessions >= maxSessions) {
            log.warn("User {} đã đạt giới hạn {} phiên. Revoke tất cả phiên cũ.", user.getUserName(), maxSessions);
            revokeAllTokensByUser(user);
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusSeconds((long) refreshTokenExpiryDays * 24 * 60 * 60))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));

        if (refreshToken.isRevoked()) {
            log.warn("Refresh token đã bị thu hồi. Có thể bị tấn công replay!");
            // Khi phát hiện token đã bị revoke được sử dụng lại → revoke tất cả token của user (bảo mật)
            revokeAllTokensByUser(refreshToken.getUser());
            throw new RuntimeException("Refresh token đã bị thu hồi. Tất cả phiên đã bị đăng xuất vì lý do bảo mật.");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        }

        return refreshToken;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        // Revoke token cũ
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Tạo token mới cho cùng user
        return createRefreshToken(oldToken.getUser());
    }

    /**
     * Revoke 1 refresh token cụ thể (logout)
     */
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        log.info("Đã revoke refresh token của user: {}", refreshToken.getUser().getUserName());
    }

    /**
     * Revoke tất cả refresh token của user (logout tất cả thiết bị)
     */
    @Transactional
    public void revokeAllTokensByUser(User user) {
        int count = refreshTokenRepository.revokeAllByUser(user);
        log.info("Đã revoke {} refresh token của user: {}", count, user.getUserName());
    }

    public long getRefreshTokenExpirySeconds() {
        return (long) refreshTokenExpiryDays * 24 * 60 * 60;
    }
}
