package com.springboot.spring_security.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.springboot.spring_security.DTO.req.RoleRequestDTO;
import com.springboot.spring_security.models.Permission;
import com.springboot.spring_security.models.Role;
import com.springboot.spring_security.repositories.PermissionRepository;
import com.springboot.spring_security.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public Role createRole(RoleRequestDTO request) {
        if(roleRepository.existsById(request.getRoleName())) {
            throw new RuntimeException("Role already exists");
        }
        
        Role role = new Role();
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        
        // Link string-based permission names into actual Permission entities
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());
            role.setPermissions(permissions);
        }
        
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRole(String roleName) {
        return roleRepository.findById(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public Role updateRole(String roleName, RoleRequestDTO request) {
        Role role = roleRepository.findById(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        role.setDescription(request.getDescription());
        
        if (request.getPermissions() != null) {
            List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());
            role.setPermissions(permissions);
        }
        
        return roleRepository.save(role);
    }
    
    public Role addPermissionsToRole(String roleName, List<String> permissionsToAdd) {
        Role role = roleRepository.findById(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        if (permissionsToAdd != null && !permissionsToAdd.isEmpty()) {
            List<Permission> newPermissions = permissionRepository.findAllById(permissionsToAdd);
            
            // Get existing permissions, initialize if null
            java.util.List<Permission> currentPermissions = role.getPermissions();
            if (currentPermissions == null) {
                currentPermissions = new java.util.ArrayList<>();
            }
            
            // Add only non-duplicates
            for (Permission p : newPermissions) {
                if (!currentPermissions.contains(p)) {
                    currentPermissions.add(p);
                }
            }
            role.setPermissions(currentPermissions);
        }
        
        return roleRepository.save(role);
    }

    public void deleteRole(String roleName) {
        roleRepository.deleteById(roleName);
    }
}
