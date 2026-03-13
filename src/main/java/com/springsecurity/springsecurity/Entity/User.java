package com.springsecurity.springsecurity.Entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NotEmpty(message = "Username must not be empty")
    @Size(min = 2, message = "Username must be at least 2 characters ")
    @Column(unique = true)
    private String username;

    @NotEmpty(message = "Fullname must not be empty")
    @Size(min = 2, message = "Fullname must be at least 2 characters ")
    private String fullName;

    @Email(message = "Email must be valid")
    @NotEmpty(message = "Email must not be empty")
    @Column(unique = true)
    private String email;
    @Size(min = 8, message = "Password must be at least 8 characters ")
    @NotEmpty(message = "Password must not be empty")
    private String password;

}