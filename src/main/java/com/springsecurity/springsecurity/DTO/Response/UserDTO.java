package com.springsecurity.springsecurity.DTO.Response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDTO {
    String fullName;
    String username;
    String email;

}
