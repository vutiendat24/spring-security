package com.springboot.spring_security.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.spring_security.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
}
