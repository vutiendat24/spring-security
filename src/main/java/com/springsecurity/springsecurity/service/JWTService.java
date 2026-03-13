package com.springsecurity.springsecurity.service;


import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.springsecurity.springsecurity.DTO.Common.JwtPayload;
import com.springsecurity.springsecurity.Entity.User;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JWTService {

    final Algorithm algorithm;
    final JWTVerifier verifier;

    public JWTService(@Value("${jwt.secret}") String secretKey) {
        this.algorithm = Algorithm.HMAC512(secretKey);
        this.verifier = JWT.require(algorithm)
                .withIssuer("DatVuCompany")
                .build();
    }


    // tạo một access token khi login thành công 
    public String generateAccessToken(User user) {
        long expiredTime = 5 * 60; // token có thời hạn 5 phút
        return JWT.create()
                .withIssuer("DatVuCompany")
                .withSubject(user.getFullName())
                .withClaim("userID", user.getId().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plusSeconds(expiredTime)))
                .sign(algorithm);
    }

    // kiểm tra token có hợp lệ hay không 
    // 1. Token có chữ ký hợp lệ
    // 2. Token chưa hết hạn
    // 3. Token được phát hành bởi server
    // 4. Token có subject hợp lệ
    public boolean verifyAccessToken(String accessToken) {
        try {
            JwtPayload payload = getPayload(accessToken);
            if(payload.getExpiresAt().before(new Date())){
                return false;
            }
            if(!payload.getIssuer().equals("DatVuCompany")){
                return false;
            }
            if(payload.getSubject() == null){
                return false;
            }
            return true;
        } catch (TokenExpiredException e) {
            log.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (JWTVerificationException e) {
            log.error("Token verification failed: {}", e.getMessage());
            return false;
        }
    }
    public JwtPayload getPayload(String token){
        DecodedJWT decodedJWT = verifier.verify(token);
        return JwtPayload.builder()
                .fullName(decodedJWT.getClaim("fullName").asString())
                .email(decodedJWT.getClaim("email").asString())
                .role(decodedJWT.getClaim("role").asString())
                .issuer(decodedJWT.getIssuer())
                .subject(decodedJWT.getSubject())
                .issuedAt(decodedJWT.getIssuedAt())
                .expiresAt(decodedJWT.getExpiresAt())
                .build();
    }
    



}
