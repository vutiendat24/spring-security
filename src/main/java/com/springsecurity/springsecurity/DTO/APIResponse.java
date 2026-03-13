package com.springsecurity.springsecurity.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class APIResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private int code;

  

  
}
