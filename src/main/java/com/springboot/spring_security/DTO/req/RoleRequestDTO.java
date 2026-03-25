package com.springboot.spring_security.DTO.req;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleRequestDTO {
    private String roleName;
    private String description;
    private List<String> permissions;
}
