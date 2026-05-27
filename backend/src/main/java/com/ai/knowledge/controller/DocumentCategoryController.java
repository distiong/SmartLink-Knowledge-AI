package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.DocumentCategory;
import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.SysUserRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.DocumentCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/category")
@CrossOrigin(origins = "*")
public class DocumentCategoryController {

    @Autowired
    private DocumentCategoryService categoryService;

    @Autowired
    private SysUserRepository userRepository;

    @GetMapping("/list")
    public ApiResponse<List<DocumentCategory>> listCategories() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            List<DocumentCategory> categories;

            if (user != null && user.getUserType() == 1) {
                categories = categoryService.getAllCategories();
            } else {
                categories = categoryService.getCategoriesByUserId(currentUser.getUserId());
            }

            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error("获取分类列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/tree")
    public ApiResponse<List<DocumentCategory>> getCategoryTree() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            List<DocumentCategory> categories;

            if (user != null && user.getUserType() == 1) {
                categories = categoryService.getAllCategories();
            } else {
                categories = categoryService.getCategoriesByUserId(currentUser.getUserId());
            }

            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error("获取分类树失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<DocumentCategory> createCategory(@RequestBody DocumentCategory category) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            category.setUserId(currentUser.getUserId());
            DocumentCategory created = categoryService.createCategory(category);
            return ApiResponse.success(created);
        } catch (Exception e) {
            return ApiResponse.error("创建分类失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<DocumentCategory> updateCategory(@PathVariable Long id, @RequestBody DocumentCategory category) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            DocumentCategory existing = categoryService.getCategoryById(id);
            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);

            if (user != null && user.getUserType() != 1 && !existing.getUserId().equals(currentUser.getUserId())) {
                return ApiResponse.error("权限不足，无法修改他人分类");
            }

            DocumentCategory updated = categoryService.updateCategory(id, category);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error("更新分类失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCategory(@PathVariable Long id) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            DocumentCategory existing = categoryService.getCategoryById(id);
            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);

            if (user != null && user.getUserType() != 1 && !existing.getUserId().equals(currentUser.getUserId())) {
                return ApiResponse.error("权限不足，无法删除他人分类");
            }

            categoryService.deleteCategory(id);
            return ApiResponse.success("分类删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("分类删除失败: " + e.getMessage());
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
