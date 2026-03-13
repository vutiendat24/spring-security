package com.springsecurity.springsecurity.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springsecurity.springsecurity.DTO.Response.UserDTO;
import com.springsecurity.springsecurity.Entity.User;
import com.springsecurity.springsecurity.repository.UserRepository;
import com.springsecurity.springsecurity.ultil.Convert;

@Service
public class UserService {

  @Autowired
  UserRepository userRepository;


  public List<UserDTO> getAllUsers(){
    List<User> listAllUsers = userRepository.findAll();

    return listAllUsers.stream()
            .map(user -> Convert.convertUserToUserDTO(user))
            .toList();
  }
  
}
