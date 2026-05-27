package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.dto.ChatResponse;
import com.ai.knowledge.dto.SearchResult;
import com.ai.knowledge.entity.ChatRecord;
import com.ai.knowledge.entity.ChatSession;
import com.ai.knowledge.entity.Document;
import com.ai.knowledge.repository.ChatRecordRepository;
import com.ai.knowledge.repository.ChatSessionRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.AiService;
import com.ai.knowledge.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class RagController {

    private static final int MAX_CONTEXT_MESSAGES = 10;

    @Autowired
    private RagService ragService;

    @Autowired
    private AiService aiService;

    @Autowired
    private ChatRecordRepository chatRecordRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @PostMapping("/upload")
    public ApiResponse<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tags) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            Document document = ragService.uploadDocument(file, currentUser.getUserId(), categoryId, tags);
            return ApiResponse.success("文档上传成功", document);
        } catch (Exception e) {
            return ApiResponse.error("文档上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/chat")
    public ApiResponse<Map<String, Object>> ragChat(
            @RequestParam String question,
            @RequestParam(required = false) Long sessionId) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            if (sessionId == null) {
                ChatSession session = new ChatSession();
                session.setUserId(currentUser.getUserId());
                session.setTitle(question.length() > 20 ? question.substring(0, 20) + "..." : question);
                session.setChatType("rag");
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

            ChatResponse response = ragService.ragChatWithContext(question, currentUser.getUserId(), context);

            ChatRecord record = new ChatRecord();
            record.setUserId(currentUser.getUserId());
            record.setSessionId(sessionId);
            record.setQuestion(question);
            record.setAnswer(response.getAnswer());
            record.setChatType("rag");
            chatRecordRepository.save(record);

            ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
            if (session != null) {
                session.setMessageCount(session.getMessageCount() + 1);
                chatSessionRepository.save(session);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("answer", response.getAnswer());
            result.put("context", response.getContext());
            result.put("sessionId", sessionId);

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("RAG问答失败: " + e.getMessage());
        }
    }

    @GetMapping("/sessions")
    public ApiResponse<List<ChatSession>> getRagSessions() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            List<ChatSession> sessions = chatSessionRepository.findByUserIdAndChatTypeOrderByUpdateTimeDesc(
                    currentUser.getUserId(), "rag");
            return ApiResponse.success(sessions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<ChatRecord>> getRagSessionMessages(@PathVariable Long sessionId) {
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

    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> deleteRagSession(@PathVariable Long sessionId) {
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

    @GetMapping("/search")
    public ApiResponse<List<SearchResult>> search(@RequestParam String query,
                                                  @RequestParam(defaultValue = "5") int topK) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            List<SearchResult> results = ragService.search(query, topK, currentUser.getUserId());
            return ApiResponse.success(results);
        } catch (Exception e) {
            return ApiResponse.error("向量检索失败: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ApiResponse<List<ChatRecord>> getRagHistory() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            List<ChatRecord> records = chatRecordRepository.findByUserIdAndChatTypeOrderByCreateTimeDesc(
                    currentUser.getUserId(), "rag");
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
