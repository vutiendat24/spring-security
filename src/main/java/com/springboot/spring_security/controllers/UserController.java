package com.springboot.spring_security.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.springboot.spring_security.DTO.res.UserDTO;
import com.springboot.spring_security.models.User;
import com.springboot.spring_security.services.UserService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public UserDTO createUser(@Valid @RequestBody User user){
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public String loginUser(@RequestBody User user){
        return userService.login(user.getUserName(), user.getPassword());
    }

}
