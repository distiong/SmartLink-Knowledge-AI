package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.entity.SysUserPermission;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.PermissionService;
import com.ai.knowledge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PermissionService permissionService;
    
    @GetMapping("/list")
    public ApiResponse<List<SysUser>> getUserList() {
        try {
            List<SysUser> users = userService.getAllUsers();
            users.forEach(u -> u.setPassword(null));
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<SysUser> getUserById(@PathVariable Long id) {
        try {
            SysUser user = userService.getUserById(id);
            user.setPassword(null);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<SysUser> createUser(@RequestBody SysUser user) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            SysUser createdUser = userService.createUser(user, currentUser.getUserId());
            
            // 根据角色分配默认权限
            if (user.getRoleId() != null) {
                String roleCode = getRoleCode(user.getRoleId());
                permissionService.assignDefaultPermissions(createdUser.getId(), roleCode);
            }
            
            createdUser.setPassword(null);
            return ApiResponse.success(createdUser);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<SysUser> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        try {
            SysUser updatedUser = userService.updateUser(id, user);
            updatedUser.setPassword(null);
            return ApiResponse.success(updatedUser);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("password");
            userService.resetPassword(id, newPassword);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status/{status}")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        try {
            userService.updateStatus(id, status);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}/permissions")
    public ApiResponse<List<SysUserPermission>> getUserPermissions(@PathVariable Long id) {
        try {
            List<SysUserPermission> permissions = permissionService.getUserPermissions(id);
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/permissions")
    public ApiResponse<Void> assignUserPermissions(@PathVariable Long id, @RequestBody Map<String, List<Map<String, Object>>> request) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getUserType() != 1) {
                return ApiResponse.error("仅超级管理员可配置权限");
            }
            
            List<Map<String, Object>> permissions = request.get("permissions");
            permissionService.assignPermissions(id, permissions);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/permissions/all")
    public ApiResponse<Map<String, Object>> getAllPermissionItems() {
        try {
            return ApiResponse.success(permissionService.getAllPermissionItems());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    private JwtAuthenticationFilter.UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtAuthenticationFilter.UserInfo) {
            return (JwtAuthenticationFilter.UserInfo) authentication.getPrincipal();
        }
        return null;
    }
    
    private String getRoleCode(Long roleId) {
        switch (roleId.intValue()) {
            case 1: return "admin";
            case 2: return "operator";
            case 3: return "visitor";
            default: return "visitor";
        }
    }
}
