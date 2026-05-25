package com.ai.knowledge.service;

import com.ai.knowledge.dto.ChatResponse;
import com.ai.knowledge.dto.SearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiService {
    
    @Value("${ai.api-key:492e52a6758d4e399ccde3d091a0a7f1.mzZN5RMNW2eJuFF7}")
    private String apiKey;
    
    @Value("${ai.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String baseUrl;
    
    @Value("${ai.model:GLM-4-Flash}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private MilvusService milvusService;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    public String chat(String message, Double temperature) {
        return chatWithContext(message, new ArrayList<>(), temperature);
    }
    
    public String chatWithContext(String message, List<Map<String, String>> history, Double temperature) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", temperature);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加历史消息
            if (history != null) {
                messages.addAll(history);
            }
            
            // 添加当前用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);
            
            body.put("messages", messages);
            
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", entity, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("AI对话失败: " + e.getMessage(), e);
        }
    }
    
    public ChatResponse ragChat(String question) {
        return ragChat(question, null);
    }
    
    public ChatResponse ragChat(String question, Long userId) {
        try {
            List<Float> queryVector = embeddingService.embed(question);
            
            List<SearchResult> searchResults;
            if (userId != null) {
                searchResults = milvusService.search(queryVector.toString(), 5, userId);
            } else {
                searchResults = milvusService.search(queryVector.toString(), 5);
            }
            
            String context = searchResults.stream()
                    .map(SearchResult::getText)
                    .collect(Collectors.joining("\n\n"));
            
            String ragPrompt = """
                    基于以下参考资料回答用户问题。如果参考资料中没有相关信息，请说明无法回答。
                    
                    参考资料：
                    %s
                    
                    用户问题：%s
                    
                    请基于参考资料给出准确回答，并在回答末尾标注引用的参考资料片段。
                    """.formatted(context, question);
            
            String answer = chat(ragPrompt, 0.7);
            
            return new ChatResponse(answer, context);
        } catch (Exception e) {
            throw new RuntimeException("RAG对话失败: " + e.getMessage(), e);
        }
    }
}
