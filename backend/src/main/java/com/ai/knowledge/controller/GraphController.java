package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.dto.GraphRelation;
import com.ai.knowledge.dto.GraphQueryResult;
import com.ai.knowledge.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "*")
public class GraphController {
    
    @Autowired
    private GraphService graphService;
    
    @PostMapping("/create")
    public ApiResponse<String> createRelation(@RequestBody GraphRelation relation) {
        try {
            graphService.createRelation(relation);
            return ApiResponse.success("关系创建成功", null);
        } catch (Exception e) {
            return ApiResponse.error("关系创建失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/query")
    public ApiResponse<GraphQueryResult> queryRelations(@RequestParam String entity) {
        try {
            GraphQueryResult result = graphService.queryRelations(entity);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("图谱查询失败: " + e.getMessage());
        }
    }
}