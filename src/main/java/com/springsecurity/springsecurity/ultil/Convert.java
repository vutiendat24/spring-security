package com.springsecurity.springsecurity.ultil;

import com.springsecurity.springsecurity.DTO.Response.UserDTO;
import com.springsecurity.springsecurity.Entity.User;

public class Convert {

    public static UserDTO convertUserToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setFullName(user.getFullName());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        return userDTO;
    }

    public static User convertUserDTOToUser(UserDTO userDTO) {
        User user = new User();
        user.setFullName(userDTO.getFullName());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        return user;
    }

}
