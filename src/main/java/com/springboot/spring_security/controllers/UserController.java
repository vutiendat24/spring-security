package com.springboot.spring_security.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.springboot.spring_security.DTO.res.UserDTO;
import com.springboot.spring_security.models.User;
import com.springboot.spring_security.services.UserService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Controller
@AllArgsConstructor
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    UserService userService;


    @PostMapping("/create")
    public UserDTO createUser(@RequestBody User user){
        return userService.createUser(user);
    }

    
}
