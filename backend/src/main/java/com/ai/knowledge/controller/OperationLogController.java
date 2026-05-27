package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.OperationLog;
import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.SysUserRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/system/log")
@CrossOrigin(origins = "*")
public class OperationLogController {

    @Autowired
    private OperationLogService logService;

    @Autowired
    private SysUserRepository userRepository;

    @GetMapping("/list")
    public ApiResponse<Page<OperationLog>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可查看操作日志");
            }

            Page<OperationLog> logs;
            if (userId != null || operationType != null || startTime != null || endTime != null) {
                logs = logService.getLogsByConditions(userId, operationType, startTime, endTime, page, size);
            } else {
                logs = logService.getAllLogs(page, size);
            }

            return ApiResponse.success(logs);
        } catch (Exception e) {
            return ApiResponse.error("获取操作日志失败: " + e.getMessage());
        }
    }

    @GetMapping("/count-today")
    public ApiResponse<Long> getTodayLogCount() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            long count = logService.countTodayLogs();
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error("获取今日日志数量失败: " + e.getMessage());
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
