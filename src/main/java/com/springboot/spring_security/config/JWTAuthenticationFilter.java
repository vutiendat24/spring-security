package com.springboot.spring_security.config;

import com.nimbusds.jwt.SignedJWT;
import com.springboot.spring_security.services.JWTService;
import com.springboot.spring_security.services.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            String jwt;
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            jwt = header.substring(7);
            
            if (jwtService.validToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                SignedJWT signedJWT = SignedJWT.parse(jwt);
                String userId = signedJWT.getJWTClaimsSet().getSubject();
                String jti = signedJWT.getJWTClaimsSet().getJWTID();
                String scope = signedJWT.getJWTClaimsSet().getStringClaim("scope");

                // Kiểm tra token có bị blacklist không (theo jti)
                if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
                    log.warn("Access token jti={} đã bị blacklist", jti);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Kiểm tra user có bị revoke-all không
                if (tokenBlacklistService.isUserTokensRevoked(userId, signedJWT.getJWTClaimsSet().getIssueTime())) {
                    log.warn("Tất cả token của user={} đã bị thu hồi", userId);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (scope != null && !scope.isBlank()) {
                    authorities = Arrays.stream(scope.split(" "))
                            .map(SimpleGrantedAuthority::new)
                            .toList();
                }
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities);
                        
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            
            
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Cannot set user authentication: ", e);
            filterChain.doFilter(request, response);
        }
    }
}
