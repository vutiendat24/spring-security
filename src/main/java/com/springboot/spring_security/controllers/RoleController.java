package com.springboot.spring_security.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.springboot.spring_security.DTO.req.RoleRequestDTO;
import com.springboot.spring_security.models.Role;
import com.springboot.spring_security.services.RoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;

    @PostMapping("/create")
    public Role createRole(@RequestBody RoleRequestDTO request) {
        return roleService.createRole(request);
    }

    @GetMapping("/all")
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{roleName}")
    public Role getRole(@PathVariable String roleName) {
        return roleService.getRole(roleName);
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update/{roleName}")
    public Role updateRole(@PathVariable String roleName, @RequestBody RoleRequestDTO request) {
        return roleService.updateRole(roleName, request);
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{roleName}")
    public void deleteRole(@PathVariable String roleName) {
        roleService.deleteRole(roleName);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/add-permissions")
    public Role addPermissionsToRole(@RequestBody RoleRequestDTO request) {
        return roleService.addPermissionsToRole(request.getRoleName(), request.getPermissions());
    }
}
