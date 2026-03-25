package com.springboot.spring_security.services;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springboot.spring_security.DTO.res.UserDTO;
import com.springboot.spring_security.models.User;
import com.springboot.spring_security.repositories.UserRepository;
import com.springboot.spring_security.ultils.Mapper;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class UserService {
    UserRepository userRepository;

    public UserDTO createUser(User user){
        // check user exists
        if(userRepository.findByUserName(user.getUserName()) != null){
            throw new RuntimeException("User already exists");
        }
        // check email exists
        if(userRepository.findByEmail(user.getEmail()) != null){
            throw new RuntimeException("Email already exists");
        }
        // check phone exists
        if(userRepository.findByPhone(user.getPhone()) != null){
            throw new RuntimeException("Phone already exists");
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return Mapper.toUserDTO(userRepository.save(user));

    }

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
