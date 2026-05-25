package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.ChatRecord;
import com.ai.knowledge.entity.ChatSession;
import com.ai.knowledge.repository.ChatRecordRepository;
import com.ai.knowledge.repository.ChatSessionRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {
    
    private static final int MAX_MESSAGE_LENGTH = 4000;
    private static final int MAX_CONTEXT_MESSAGES = 20;
    
    @Autowired
    private AiService aiService;
    
    @Autowired
    private ChatRecordRepository chatRecordRepository;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            String message = (String) request.get("message");
            Long sessionId = request.get("sessionId") != null ? 
                Long.valueOf(request.get("sessionId").toString()) : null;
            Double temperature = request.get("temperature") != null ? 
                Double.valueOf(request.get("temperature").toString()) : 0.7;
            
            // 检查消息长度
            if (message != null && message.length() > MAX_MESSAGE_LENGTH) {
                return ApiResponse.error("消息长度超过限制（最大" + MAX_MESSAGE_LENGTH + "字符）");
            }
            
            // 如果没有sessionId，创建新会话
            if (sessionId == null) {
                ChatSession session = new ChatSession();
                session.setUserId(currentUser.getUserId());
                session.setTitle(message.length() > 20 ? message.substring(0, 20) + "..." : message);
                session.setChatType("chat");
                session = chatSessionRepository.save(session);
                sessionId = session.getId();
            }
            
            // 获取历史消息作为上下文
            List<ChatRecord> history = chatRecordRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
            List<Map<String, String>> context = new ArrayList<>();
            int start = Math.max(0, history.size() - MAX_CONTEXT_MESSAGES);
            for (int i = start; i < history.size(); i++) {
                ChatRecord record = history.get(i);
                Map<String, String> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", record.getQuestion());
                context.add(userMsg);
                
                Map<String, String> aiMsg = new HashMap<>();
                aiMsg.put("role", "assistant");
                aiMsg.put("content", record.getAnswer());
                context.add(aiMsg);
            }
            
            String response = aiService.chatWithContext(message, context, temperature);
            
            // 保存对话记录
            ChatRecord record = new ChatRecord();
            record.setUserId(currentUser.getUserId());
            record.setQuestion(message);
            record.setAnswer(response);
            record.setChatType("chat");
            record.setSessionId(sessionId);
            chatRecordRepository.save(record);
            
            // 更新会话
            ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
            if (session != null) {
                session.setMessageCount(session.getMessageCount() + 1);
                chatSessionRepository.save(session);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("answer", response);
            result.put("sessionId", sessionId);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("AI对话失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/sessions")
    public ApiResponse<List<ChatSession>> getSessions() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            List<ChatSession> sessions = chatSessionRepository.findByUserIdAndChatTypeOrderByUpdateTimeDesc(
                    currentUser.getUserId(), "chat");
            return ApiResponse.success(sessions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<ChatRecord>> getSessionMessages(@PathVariable Long sessionId) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            List<ChatRecord> messages = chatRecordRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
            return ApiResponse.success(messages);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/sessions")
    public ApiResponse<ChatSession> createSession() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            ChatSession session = new ChatSession();
            session.setUserId(currentUser.getUserId());
            session.setTitle("新对话");
            session.setChatType("chat");
            session = chatSessionRepository.save(session);
            
            return ApiResponse.success(session);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable Long sessionId) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            // 删除会话下的所有消息
            List<ChatRecord> messages = chatRecordRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
            chatRecordRepository.deleteAll(messages);
            
            // 删除会话
            chatSessionRepository.deleteById(sessionId);
            
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/history")
    public ApiResponse<List<ChatRecord>> getChatHistory() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            List<ChatRecord> records = chatRecordRepository.findByUserIdAndChatTypeOrderByCreateTimeDesc(
                    currentUser.getUserId(), "chat");
            return ApiResponse.success(records);
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
