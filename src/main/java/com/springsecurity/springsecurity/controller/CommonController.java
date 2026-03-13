package com.springsecurity.springsecurity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController

public class CommonController {

  @GetMapping("/")
  public String getMethodName() {
      return "URL default";
  }
  
  
}
