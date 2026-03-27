package com.springboot.spring_security.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    // Thêm access token vào blacklist với TTL = thời gian còn lại trước khi token hết hạn
    public void blacklistAccessToken(String jti, Date expirationTime) {
        long ttlSeconds = Duration.between(Instant.now(), expirationTime.toInstant()).getSeconds();

        if (ttlSeconds <= 0) {
            log.info("Token jti={} đã hết hạn, không cần blacklist", jti);
            return;
        }

        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofSeconds(ttlSeconds));
        log.info("Đã blacklist access token jti={}, TTL={}s", jti, ttlSeconds);
    }

    // Kiểm tra access token có bị blacklist không
    public boolean isBlacklisted(String jti) {
        String key = BLACKLIST_PREFIX + jti;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    // Blacklist tất cả access token của 1 user (dùng user_id làm key phụ)
    // Cơ chế: lưu thời điểm "revoke all" → mọi token phát hành trước thời điểm này đều bị coi là invalid
    public void blacklistAllTokensOfUser(String userId) {
        String key = "token:revoke_all:" + userId;
        redisTemplate.opsForValue().set(key, String.valueOf(Instant.now().toEpochMilli()),
                Duration.ofSeconds(JWTService.ACCESS_TOKEN_EXPIRY_SECONDS));
        log.info("Đã blacklist tất cả access token của user={}", userId);
    }

    // Kiểm tra token có bị revoke-all không (token phát hành trước thời điểm revoke-all → invalid)
    public boolean isUserTokensRevoked(String userId, Date tokenIssuedAt) {
        String key = "token:revoke_all:" + userId;
        String revokeTimestamp = redisTemplate.opsForValue().get(key);

        if (revokeTimestamp == null) {
            return false;
        }

        long revokeTime = Long.parseLong(revokeTimestamp);
        return tokenIssuedAt.toInstant().toEpochMilli() < revokeTime;
    }
}
