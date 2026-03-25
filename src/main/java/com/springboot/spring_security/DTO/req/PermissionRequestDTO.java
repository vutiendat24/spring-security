package com.springboot.spring_security.DTO.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionRequestDTO {
    private String permissionName;
    private String description;
}
