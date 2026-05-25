package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.dto.ChatResponse;
import com.ai.knowledge.dto.SearchResult;
import com.ai.knowledge.entity.ChatRecord;
import com.ai.knowledge.entity.Document;
import com.ai.knowledge.repository.ChatRecordRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.AiService;
import com.ai.knowledge.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class RagController {
    
    @Autowired
    private RagService ragService;
    
    @Autowired
    private AiService aiService;
    
    @Autowired
    private ChatRecordRepository chatRecordRepository;
    
    @PostMapping("/upload")
    public ApiResponse<Document> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            Document document = ragService.uploadDocument(file, currentUser.getUserId());
            return ApiResponse.success("文档上传成功", document);
        } catch (Exception e) {
            return ApiResponse.error("文档上传失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/chat")
    public ApiResponse<ChatResponse> ragChat(@RequestParam String question) {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }
            
            ChatResponse response = aiService.ragChat(question, currentUser.getUserId());
            
            // 保存RAG对话记录
            ChatRecord record = new ChatRecord();
            record.setUserId(currentUser.getUserId());
            record.setQuestion(question);
            record.setAnswer(response.getAnswer());
            record.setChatType("rag");
            chatRecordRepository.save(record);
            
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("RAG问答失败: " + e.getMessage());
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
