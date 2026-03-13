package com.springsecurity.springsecurity.config;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${jwt.secret}")
    private  String secretKey;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF để có thể test POST qua Postman dễ dàng
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Cho phép tất cả API bắt đầu bằng /auth/ truy cập tự do
                        .anyRequest().authenticated() // Các API khác vẫn bắt buộc đăng nhập
                );
        http.oauth2ResourceServer(OAuth2 ->
            OAuth2.jwt(jwtCustomizer ->jwtCustomizer.decoder(jwtDecoder()))
         );

        return http.build();
    }
    @Bean
    JwtDecoder jwtDecoder(){
       
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HMAC512");
        return NimbusJwtDecoder 
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}