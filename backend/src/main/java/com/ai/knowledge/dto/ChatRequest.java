package com.ai.knowledge.dto;

import lombok.Data;

@Data
public class ChatRequest {
    
    private String message;
    private Double temperature;
    private String model;
}