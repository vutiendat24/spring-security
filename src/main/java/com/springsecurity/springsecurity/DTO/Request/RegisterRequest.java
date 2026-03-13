package com.springsecurity.springsecurity.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotEmpty(message = "FULLNAME IS REQUIRED")
    String fullName;
    @NotEmpty(message = "USERNAME IS REQUIRED")
    @Size(min = 2, message = "USERNAME MUST BE AT LEAST 2 CHARACTERS")
    String username;
    @NotEmpty(message = "EMAIL IS REQUIRED")
    @Email(message = "EMAIL IS INVALID")
    String email;
    @NotEmpty(message = "PASSWORD IS REQUIRED")
    @Size(min = 8, message = "PASSWORD MUST BE AT LEAST 8 CHARACTERS")
    String password;
}
