package com.springsecurity.springsecurity.DTO.Common;

import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtPayload{
    private String fullName;
    private String email;
    private String role;
    private String issuer;
    private String subject;
    private Date issuedAt;
    private Date expiresAt;
}
