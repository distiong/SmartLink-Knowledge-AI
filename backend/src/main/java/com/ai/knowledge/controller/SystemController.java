package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.*;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChatRecordRepository chatRecordRepository;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboardData() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }

            Map<String, Object> data = new HashMap<>();

            boolean isAdmin = user.getUserType() == 1;

            if (isAdmin) {
                data.put("totalDocuments", documentRepository.count());
                data.put("totalChatRecords", chatRecordRepository.count());
                data.put("totalUsers", userRepository.count());
                data.put("todayLogs", operationLogService.countTodayLogs());
            } else {
                data.put("totalDocuments", documentRepository.findByUserId(currentUser.getUserId()).size());
                data.put("totalChatRecords", chatRecordRepository.findByUserIdOrderByCreateTimeDesc(currentUser.getUserId()).size());
                data.put("totalUsers", 1L);
                data.put("todayLogs", 0L);
            }

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("获取统计数据失败: " + e.getMessage());
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
