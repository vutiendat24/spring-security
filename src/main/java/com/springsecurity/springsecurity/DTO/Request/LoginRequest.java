package com.springsecurity.springsecurity.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class LoginRequest {
  @NotEmpty(message = "EMAIL IS REQUIRED")
  @Email(message = "EMAIL IS INVALID")
  String email;
  @NotEmpty(message = "PASSWORD IS REQUIRED")
  String password;
}
