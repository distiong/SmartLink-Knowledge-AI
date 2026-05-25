package com.ai.knowledge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {
    
    @Value("${neo4j.enabled:false}")
    private boolean neo4jEnabled;
}