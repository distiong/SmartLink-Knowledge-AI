package com.ai.knowledge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class EmbeddingService {
    
    @Value("${milvus.service.url:http://localhost:8081}")
    private String milvusServiceUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<Float> embed(String text) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("text", text);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
            Map response = restTemplate.postForObject(
                    milvusServiceUrl + "/embed", entity, Map.class);
            
            if (response != null) {
                List<Double> embedding = (List<Double>) response.get("embedding");
                List<Float> result = new ArrayList<>();
                for (Double d : embedding) {
                    result.add(d.floatValue());
                }
                return result;
            }
            
            throw new RuntimeException("Failed to get embedding");
        } catch (Exception e) {
            throw new RuntimeException("Failed to embed text: " + e.getMessage(), e);
        }
    }
    
    public List<List<Float>> batchEmbed(List<String> texts) {
        try {
            Map<String, List<String>> request = new HashMap<>();
            request.put("texts", texts);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, List<String>>> entity = new HttpEntity<>(request, headers);
            Map response = restTemplate.postForObject(
                    milvusServiceUrl + "/batch_embed", entity, Map.class);
            
            if (response != null) {
                List<List<Double>> embeddings = (List<List<Double>>) response.get("embeddings");
                List<List<Float>> result = new ArrayList<>();
                for (List<Double> embedding : embeddings) {
                    List<Float> floatList = new ArrayList<>();
                    for (Double d : embedding) {
                        floatList.add(d.floatValue());
                    }
                    result.add(floatList);
                }
                return result;
            }
            
            throw new RuntimeException("Failed to get embeddings");
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch embed texts: " + e.getMessage(), e);
        }
    }
}