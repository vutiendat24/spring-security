package com.springboot.spring_security.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.springboot.spring_security.DTO.req.PermissionRequestDTO;
import com.springboot.spring_security.models.Permission;
import com.springboot.spring_security.repositories.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public Permission createPermission(PermissionRequestDTO request) {
        if(permissionRepository.existsById(request.getPermissionName())) {
            throw new RuntimeException("Permission already exists");
        }
        Permission permission = new Permission();
        permission.setPermissionName(request.getPermissionName());
        permission.setDescription(request.getDescription());
        return permissionRepository.save(permission);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Permission getPermission(String permissionName) {
        return permissionRepository.findById(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
    }

    public Permission updatePermission(String permissionName, PermissionRequestDTO request) {
        Permission permission = permissionRepository.findById(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        permission.setDescription(request.getDescription());
        return permissionRepository.save(permission);
    }

    public void deletePermission(String permissionName) {
        permissionRepository.deleteById(permissionName);
    }
}
