package com.springboot.spring_security.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;
import com.springboot.spring_security.enums.UserStatus;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID userID;

    @Size(min = 5, message="User name must be at least 5 characters")
    String userName;

    @Size(min = 8, message="Password must be at least 8 characters")
    String password;

    @Size(min=2, message="Full name must be at least 2 characters")
    String fullName;

    String phone;

    @Email(message="Email is not valid")
    String email;

    String sex;

    @ManyToMany
    List<Role> roles;

    boolean isActive = false;
    
    @Enumerated(EnumType.STRING)
    UserStatus status;
}
