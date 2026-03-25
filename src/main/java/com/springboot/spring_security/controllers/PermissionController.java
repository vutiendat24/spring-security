package com.springboot.spring_security.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.springboot.spring_security.DTO.req.PermissionRequestDTO;
import com.springboot.spring_security.models.Permission;
import com.springboot.spring_security.services.PermissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionService permissionService;

    @PostMapping("/create")
    public Permission createPermission(@RequestBody PermissionRequestDTO request) {
        return permissionService.createPermission(request);
    }

    @GetMapping("/all")
    public List<Permission> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    @GetMapping("/{permissionName}")
    public Permission getPermission(@PathVariable String permissionName) {
        return permissionService.getPermission(permissionName);
    }

    @PutMapping("/update")
    public Permission updatePermission(@RequestBody PermissionRequestDTO request) {
        return permissionService.updatePermission(request.getPermissionName(), request);
    }

    @DeleteMapping("/delete")
    public void deletePermission(@RequestBody PermissionRequestDTO request) {
        permissionService.deletePermission(request.getPermissionName());
    }
}
