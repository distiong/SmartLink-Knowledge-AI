package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.ModelConfig;
import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.SysUserRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.ModelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/system/model")
@CrossOrigin(origins = "*")
public class ModelConfigController {

    @Autowired
    private ModelConfigService modelConfigService;

    @Autowired
    private SysUserRepository userRepository;

    @GetMapping("/list")
    public ApiResponse<List<ModelConfig>> getAllModels() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可管理模型配置");
            }

            List<ModelConfig> models = modelConfigService.getAllModels();
            return ApiResponse.success(models);
        } catch (Exception e) {
            return ApiResponse.error("获取模型列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/enabled")
    public ApiResponse<List<ModelConfig>> getEnabledModels() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            List<ModelConfig> models = modelConfigService.getEnabledModels();
            return ApiResponse.success(models);
        } catch (Exception e) {
            return ApiResponse.error("获取启用模型列表失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<ModelConfig> createModel(@RequestBody ModelConfig model) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可创建模型配置");
            }

            ModelConfig created = modelConfigService.createModel(model);
            return ApiResponse.success(created);
        } catch (Exception e) {
            return ApiResponse.error("创建模型配置失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<ModelConfig> updateModel(@PathVariable Long id, @RequestBody ModelConfig model) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可更新模型配置");
            }

            ModelConfig updated = modelConfigService.updateModel(id, model);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error("更新模型配置失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteModel(@PathVariable Long id) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可删除模型配置");
            }

            modelConfigService.deleteModel(id);
            return ApiResponse.success("模型配置删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("删除模型配置失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle")
    public ApiResponse<ModelConfig> toggleModel(@PathVariable Long id) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可切换模型状态");
            }

            ModelConfig toggled = modelConfigService.toggleModel(id);
            return ApiResponse.success(toggled);
        } catch (Exception e) {
            return ApiResponse.error("切换模型状态失败: " + e.getMessage());
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
