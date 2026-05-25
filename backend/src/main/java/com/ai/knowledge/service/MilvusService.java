package com.ai.knowledge.service;

import com.ai.knowledge.dto.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class MilvusService {
    
    @Value("${milvus.service.url:http://localhost:8081}")
    private String milvusServiceUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void insertVectors(Long documentId, List<String> chunks, List<List<Float>> vectors) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("document_id", documentId);
            request.put("chunks", chunks);
            request.put("vectors", vectors);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.postForObject(milvusServiceUrl + "/insert", entity, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert vectors: " + e.getMessage(), e);
        }
    }
    
    public List<SearchResult> search(String queryVector, int topK) {
        return search(queryVector, topK, null);
    }
    
    public List<SearchResult> search(String queryVector, int topK, Long userId) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("query_vector", parseVector(queryVector));
            request.put("top_k", topK);
            if (userId != null) {
                request.put("user_id", userId);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            Map response = restTemplate.postForObject(
                    milvusServiceUrl + "/search", entity, Map.class);
            
            List<SearchResult> results = new ArrayList<>();
            if (response != null) {
                List<Map<String, Object>> searchResults = (List<Map<String, Object>>) response.get("results");
                if (searchResults != null) {
                    for (Map<String, Object> result : searchResults) {
                        String text = (String) result.get("text");
                        Double score = ((Number) result.get("score")).doubleValue();
                        Long docId = ((Number) result.get("document_id")).longValue();
                        results.add(new SearchResult(text, score, docId));
                    }
                }
            }
            
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search vectors: " + e.getMessage(), e);
        }
    }
    
    public void deleteByDocumentId(Long documentId) {
        try {
            restTemplate.delete(milvusServiceUrl + "/delete/" + documentId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete vectors: " + e.getMessage(), e);
        }
    }
    
    private List<Float> parseVector(String vectorStr) {
        List<Float> vector = new ArrayList<>();
        String cleaned = vectorStr.replaceAll("[\\[\\]\\s]", "");
        for (String s : cleaned.split(",")) {
            vector.add(Float.parseFloat(s.trim()));
        }
        return vector;
    }
}
