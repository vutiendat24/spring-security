package com.springboot.spring_security.models;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Role {
    @Id
    private String roleName;
    private String description;

    @ManyToMany
    private List<Permission> permissions;
}
