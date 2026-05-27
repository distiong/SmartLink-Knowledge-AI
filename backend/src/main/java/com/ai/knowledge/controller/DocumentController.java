package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.Document;
import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.SysUserRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.DocumentService;
import com.ai.knowledge.annotation.OperationLog;
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
    public ApiResponse<List<Document>> listDocuments(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tag) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            List<Document> documents;

            if (categoryId != null) {
                if (user != null && user.getUserType() == 1) {
                    documents = documentService.getDocumentsByCategory(categoryId);
                } else {
                    documents = documentService.getDocumentsByUserIdAndCategory(currentUser.getUserId(), categoryId);
                }
            } else if (tag != null && !tag.isEmpty()) {
                if (user != null && user.getUserType() == 1) {
                    documents = documentService.getDocumentsByTag(null, tag);
                } else {
                    documents = documentService.getDocumentsByTag(currentUser.getUserId(), tag);
                }
            } else {
                if (user != null && user.getUserType() == 1) {
                    documents = documentService.getAllDocuments();
                } else {
                    documents = documentService.getDocumentsByUserId(currentUser.getUserId());
                }
            }

            return ApiResponse.success(documents);
        } catch (Exception e) {
            return ApiResponse.error("获取文档列表失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @OperationLog(operation = "删除文档", type = "删除")
    public ApiResponse<String> deleteDocument(@RequestParam Long id) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            Document doc = documentService.getDocumentById(id);
            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);

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
