package com.springboot.spring_security.repositories;

import com.springboot.spring_security.models.RefreshToken;
import com.springboot.spring_security.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    // Revoke tất cả refresh token của 1 user (dùng cho logout-all)
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
    int revokeAllByUser(User user);

    // Đếm số phiên đang hoạt động của user
    long countByUserAndRevokedFalse(User user);

    // Xóa các token đã hết hạn (dùng cho scheduled cleanup)
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < CURRENT_TIMESTAMP")
    int deleteExpiredTokens();
}
