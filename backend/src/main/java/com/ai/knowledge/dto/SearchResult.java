package com.ai.knowledge.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {

    private String text;
    private Double score;
    private Long documentId;
    private String fileName;
    private Integer chunkIndex;
    private Integer pageNumber;
}