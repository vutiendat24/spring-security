package com.springboot.spring_security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.spring_security.models.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
