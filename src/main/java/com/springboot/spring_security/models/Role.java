package com.springboot.spring_security.models;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    private String roleName;
    private String description;

    @ManyToMany
    private List<Permission> permissions;
}
