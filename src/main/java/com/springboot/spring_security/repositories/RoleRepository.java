package com.springboot.spring_security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.spring_security.models.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByRoleName(String roleName);
}
