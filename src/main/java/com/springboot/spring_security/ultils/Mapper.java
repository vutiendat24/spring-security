package com.springboot.spring_security.ultils;

import com.springboot.spring_security.DTO.res.UserDTO;
import com.springboot.spring_security.models.User;

public class Mapper {
    public static UserDTO toUserDTO(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(user.getUserName());
        userDTO.setFullName(user.getFullName());
        userDTO.setPhone(user.getPhone());
        userDTO.setSex(user.getSex());
        userDTO.setRoles(user.getRoles());
        return userDTO;
    }
    public static User toUser(UserDTO userDTO){
        User user = new User();
        user.setUserName(userDTO.getUserName());
        user.setFullName(userDTO.getFullName());
        user.setPhone(userDTO.getPhone());
        user.setSex(userDTO.getSex());
        user.setRoles(userDTO.getRoles());
        return user;
    }
}
