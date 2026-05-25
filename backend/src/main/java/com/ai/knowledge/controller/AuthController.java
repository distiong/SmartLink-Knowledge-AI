package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.entity.SysUserPermission;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.AuthService;
import com.ai.knowledge.service.MenuService;
import com.ai.knowledge.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private PermissionService permissionService;
    
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            Map<String, Object> result = authService.login(username, password);
            
            // 获取用户权限列表
            Long userId = (Long) result.get("userId");
            List<String> permCodes = permissionService.getUserPermCodes(userId);
            result.put("permissions", permCodes);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String nickname = request.get("nickname");
            
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            Long createUser = currentUser != null ? currentUser.getUserId() : 0L;
            
            Map<String, Object> result = authService.register(username, password, nickname, createUser);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getUserInfo() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            SysUser user = authService.getCurrentUser(currentUser.getUserId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("nickname", user.getNickname());
            result.put("userType", user.getUserType());
            result.put("roleId", user.getRoleId());
            
            // 获取用户权限列表
            List<String> permCodes = permissionService.getUserPermCodes(user.getId());
            result.put("permissions", permCodes);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/menu")
    public ApiResponse<List<Map<String, Object>>> getUserMenus() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            SysUser user = authService.getCurrentUser(currentUser.getUserId());
            
            // 超级管理员获取所有菜单
            List<Map<String, Object>> menus;
            if (user.getUserType() == 1) {
                menus = menuService.getAllMenusAsTree();
            } else {
                // 子账号根据权限获取菜单
                List<String> permCodes = permissionService.getUserPermCodes(user.getId());
                menus = menuService.getMenusByPermissions(permCodes);
            }
            
            return ApiResponse.success(menus);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/permissions")
    public ApiResponse<List<String>> getUserPermissions() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            List<String> permissions = permissionService.getUserPermCodes(currentUser.getUserId());
            
            return ApiResponse.success(permissions);
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
}
