package com.springboot.spring_security.models;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Permission {
    @Id
    private String permissionName;
    private String description;
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private List<Role> roles;
}