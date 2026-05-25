package com.ai.knowledge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {
    
    @Value("${milvus.service.url:http://localhost:8081}")
    private String milvusServiceUrl;
}