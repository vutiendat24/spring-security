package com.springboot.spring_security.services;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.springboot.spring_security.models.User;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class JWTService {
    @Value("${jwt.secret_key}")
    String SECRET_KEY;

    public String generateAccessToken(User user){
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .issuer("TienDatCompany")
                    .subject(user.getUserID().toString())
                    .claim("role","USER")
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES)))
                    .build();
            SignedJWT signedJWT = new SignedJWT(header,claimsSet);
            JWSSigner jwsSigner = new MACSigner(SECRET_KEY.getBytes());
            signedJWT.sign(jwsSigner);
            return signedJWT.serialize();
        } catch (KeyLengthException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean validToken(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier jwsVerifier = new MACVerifier(SECRET_KEY.getBytes());

//        check chữ ký có hợp lệ hay ko
        if(!signedJWT.verify(jwsVerifier)){
            log.error("Token have signature invalid");
            return false;
        }
//        check token còn hạn hay ko
        Date expriredTime = Date.from(Instant.now());
        if(signedJWT.getJWTClaimsSet().getExpirationTime().before(expriredTime)){
            log.error("Token was exprired");
            return false;
        }

        return true;
    }

}
