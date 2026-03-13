package com.springsecurity.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springsecurity.springsecurity.DTO.APIResponse;
import com.springsecurity.springsecurity.DTO.Request.IntrospectRequest;
import com.springsecurity.springsecurity.DTO.Request.LoginRequest;
import com.springsecurity.springsecurity.DTO.Request.RegisterRequest;
import com.springsecurity.springsecurity.DTO.Response.IntrospectResponse;
import com.springsecurity.springsecurity.DTO.Response.LoginResponse;
import com.springsecurity.springsecurity.DTO.Response.UserDTO;
import com.springsecurity.springsecurity.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

  @Autowired
  private AuthService userService;

  @GetMapping("/")
  public String getMethodName(@RequestParam String param) {
    return "test api auth";
  }

  @PostMapping("/login")
  public APIResponse<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
    String token = userService.login(loginRequest);

    LoginResponse loginResponse = LoginResponse.builder()
        .token(token)
        .build();

    APIResponse<LoginResponse> apiResponse = new APIResponse<>();
    apiResponse.setSuccess(true);
    apiResponse.setMessage("Login success");
    apiResponse.setData(loginResponse);
    apiResponse.setCode(1000);
    return apiResponse;
  }

  @PostMapping("/register")
  public APIResponse<UserDTO> register(@RequestBody @Valid RegisterRequest registerRequest) {
    UserDTO userDTO = userService.register(registerRequest);

    APIResponse<UserDTO> apiResponse = new APIResponse<>();
    apiResponse.setSuccess(true);
    apiResponse.setMessage("Register success");
    apiResponse.setData(userDTO);
    apiResponse.setCode(1000);
    return apiResponse;
  }

  @PostMapping("/introspect")
  public APIResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest introspectRequest) {
    IntrospectResponse introspectResponse = userService.introspect(introspectRequest);

    APIResponse<IntrospectResponse> apiResponse = new APIResponse<>();
    apiResponse.setSuccess(true);
    apiResponse.setMessage("Introspect success");
    apiResponse.setData(introspectResponse);
    apiResponse.setCode(1000);
    return apiResponse;
  }

}


