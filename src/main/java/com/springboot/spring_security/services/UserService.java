package com.springboot.spring_security.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.springboot.spring_security.DTO.res.UserDTO;
import com.springboot.spring_security.repositories.UserRepository;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class UserService {
    UserRepository userRepository;

    public UserDTO getUserById(UUID id){
        return userRepository.findById(id).map(user -> {
            UserDTO userDTO = new UserDTO();
            userDTO.setUserName(user.getUserName());
            userDTO.setFullName(user.getFullName());
            userDTO.setPhone(user.getPhone());
            userDTO.setSex(user.getSex());
            userDTO.setRoles(user.getRoles());
            return userDTO;
        }).orElse(null);
    }
    
}
