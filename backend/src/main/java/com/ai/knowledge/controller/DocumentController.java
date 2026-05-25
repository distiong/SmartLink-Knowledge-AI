package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.Document;
import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.SysUserRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/document")
@CrossOrigin(origins = "*")
public class DocumentController {
    
    @Autowired
    private DocumentService documentService;
    
    @Autowired
    private SysUserRepository userRepository;
    
    @GetMapping("/list")
    public ApiResponse<List<Document>> listDocuments() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            List<Document> documents;
            
            // 超级管理员查看所有文档，子账号只查看自己的文档
            if (user != null && user.getUserType() == 1) {
                documents = documentService.getAllDocuments();
            } else {
                documents = documentService.getDocumentsByUserId(currentUser.getUserId());
            }
            
            return ApiResponse.success(documents);
        } catch (Exception e) {
            return ApiResponse.error("获取文档列表失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete")
    public ApiResponse<String> deleteDocument(@RequestParam Long id) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            Document doc = documentService.getDocumentById(id);
            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            
            // 权限检查：只能删除自己的文档，超级管理员可以删除所有
            if (user != null && user.getUserType() != 1 && !doc.getUserId().equals(currentUser.getUserId())) {
                return ApiResponse.error("权限不足，无法删除他人文档");
            }
            
            documentService.deleteDocument(id);
            return ApiResponse.success("文档删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("文档删除失败: " + e.getMessage());
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
