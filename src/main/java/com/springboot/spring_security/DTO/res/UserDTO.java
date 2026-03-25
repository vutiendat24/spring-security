package com.springboot.spring_security.DTO.res;

import java.util.List;

import com.springboot.spring_security.models.Role;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDTO {
    private String userName;
    private String fullName;
    private String phone;
    private String sex;
    private List<Role> roles;
}
