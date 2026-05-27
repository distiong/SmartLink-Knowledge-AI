package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.SysConfig;
import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.SysUserRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/config")
@CrossOrigin(origins = "*")
public class SysConfigController {

    @Autowired
    private SysConfigService configService;

    @Autowired
    private SysUserRepository userRepository;

    @GetMapping("/list")
    public ApiResponse<List<SysConfig>> getAllConfigs() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可查看系统配置");
            }

            List<SysConfig> configs = configService.getAllConfigs();
            return ApiResponse.success(configs);
        } catch (Exception e) {
            return ApiResponse.error("获取配置列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{key}")
    public ApiResponse<SysConfig> getConfigByKey(@PathVariable String key) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            return configService.getConfigByKey(key)
                    .map(ApiResponse::success)
                    .orElse(ApiResponse.error("配置不存在"));
        } catch (Exception e) {
            return ApiResponse.error("获取配置失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<SysConfig> createConfig(@RequestBody SysConfig config) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可创建配置");
            }

            SysConfig created = configService.createConfig(config);
            return ApiResponse.success(created);
        } catch (Exception e) {
            return ApiResponse.error("创建配置失败: " + e.getMessage());
        }
    }

    @PutMapping("/{key}")
    public ApiResponse<SysConfig> updateConfig(@PathVariable String key, @RequestBody Map<String, String> request) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可更新配置");
            }

            String value = request.get("value");
            SysConfig updated = configService.updateConfig(key, value);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error("更新配置失败: " + e.getMessage());
        }
    }

    @PutMapping("/batch")
    public ApiResponse<String> batchUpdateConfigs(@RequestBody Map<String, String> configs) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可更新配置");
            }

            for (Map.Entry<String, String> entry : configs.entrySet()) {
                configService.updateConfig(entry.getKey(), entry.getValue());
            }

            return ApiResponse.success("批量更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error("批量更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{key}")
    public ApiResponse<String> deleteConfig(@PathVariable String key) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可删除配置");
            }

            configService.deleteConfig(key);
            return ApiResponse.success("配置删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("删除配置失败: " + e.getMessage());
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
