package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.ChatRecord;
import com.ai.knowledge.entity.ChatSession;
import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.ChatRecordRepository;
import com.ai.knowledge.repository.ChatSessionRepository;
import com.ai.knowledge.repository.SysUserRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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

    @Autowired
    private SysUserRepository userRepository;

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

            if (message != null && message.length() > MAX_MESSAGE_LENGTH) {
                return ApiResponse.error("消息长度超过限制（最大" + MAX_MESSAGE_LENGTH + "字符）");
            }

            if (sessionId == null) {
                ChatSession session = new ChatSession();
                session.setUserId(currentUser.getUserId());
                session.setTitle(message.length() > 20 ? message.substring(0, 20) + "..." : message);
                session.setChatType("chat");
                session = chatSessionRepository.save(session);
                sessionId = session.getId();
            }

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

            ChatRecord record = new ChatRecord();
            record.setUserId(currentUser.getUserId());
            record.setQuestion(message);
            record.setAnswer(response);
            record.setChatType("chat");
            record.setSessionId(sessionId);
            chatRecordRepository.save(record);

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

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> request) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                SseEmitter errorEmitter = new SseEmitter();
                errorEmitter.completeWithError(new RuntimeException("未登录"));
                return errorEmitter;
            }

            String message = (String) request.get("message");
            Long sessionId = request.get("sessionId") != null ?
                Long.valueOf(request.get("sessionId").toString()) : null;
            Double temperature = request.get("temperature") != null ?
                Double.valueOf(request.get("temperature").toString()) : 0.7;

            if (sessionId == null) {
                ChatSession session = new ChatSession();
                session.setUserId(currentUser.getUserId());
                session.setTitle(message.length() > 20 ? message.substring(0, 20) + "..." : message);
                session.setChatType("chat");
                session = chatSessionRepository.save(session);
                sessionId = session.getId();
            }

            Long finalSessionId = sessionId;
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

            return aiService.chatWithSse(message, context, temperature);
        } catch (Exception e) {
            SseEmitter errorEmitter = new SseEmitter();
            errorEmitter.completeWithError(e);
            return errorEmitter;
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

            List<ChatRecord> messages = chatRecordRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
            chatRecordRepository.deleteAll(messages);

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

    @PostMapping("/feedback")
    public ApiResponse<String> submitFeedback(@RequestBody Map<String, Object> request) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            Long recordId = Long.valueOf(request.get("recordId").toString());
            Integer feedback = Integer.valueOf(request.get("feedback").toString());

            ChatRecord record = chatRecordRepository.findById(recordId).orElse(null);
            if (record == null) {
                return ApiResponse.error("对话记录不存在");
            }

            if (!record.getUserId().equals(currentUser.getUserId())) {
                return ApiResponse.error("权限不足");
            }

            record.setFeedback(feedback);
            chatRecordRepository.save(record);

            return ApiResponse.success("反馈成功", null);
        } catch (Exception e) {
            return ApiResponse.error("反馈失败: " + e.getMessage());
        }
    }

    @GetMapping("/feedback/stats")
    public ApiResponse<Map<String, Object>> getFeedbackStats() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            if (user == null || user.getUserType() != 1) {
                return ApiResponse.error("权限不足，仅管理员可查看反馈统计");
            }

            List<ChatRecord> allRecords = chatRecordRepository.findAll();
            long totalCount = allRecords.size();
            long likeCount = allRecords.stream().filter(r -> r.getFeedback() != null && r.getFeedback() == 1).count();
            long dislikeCount = allRecords.stream().filter(r -> r.getFeedback() != null && r.getFeedback() == 2).count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCount", totalCount);
            stats.put("likeCount", likeCount);
            stats.put("dislikeCount", dislikeCount);
            stats.put("satisfactionRate", totalCount > 0 ? (double) likeCount / totalCount * 100 : 0);

            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("获取反馈统计失败: " + e.getMessage());
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
