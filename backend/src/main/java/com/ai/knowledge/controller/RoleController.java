package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.SysRole;
import com.ai.knowledge.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
@CrossOrigin(origins = "*")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    @GetMapping("/list")
    public ApiResponse<List<SysRole>> getRoleList() {
        try {
            return ApiResponse.success(roleService.getAllRoles());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getRoleById(@PathVariable Long id) {
        try {
            return ApiResponse.success(roleService.getRoleWithMenus(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<SysRole> createRole(@RequestBody SysRole role) {
        try {
            return ApiResponse.success(roleService.createRole(role));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<SysRole> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        try {
            return ApiResponse.success(roleService.updateRole(id, role));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/menus")
    public ApiResponse<Void> assignMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> menuIds = request.get("menuIds");
            roleService.assignMenus(id, menuIds);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}/menus")
    public ApiResponse<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        try {
            return ApiResponse.success(roleService.getMenuIdsByRoleId(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
