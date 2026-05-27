package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.dto.GraphRelation;
import com.ai.knowledge.dto.GraphQueryResult;
import com.ai.knowledge.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/reasoning")
    public ApiResponse<GraphQueryResult> multiHopReasoning(
            @RequestParam String entity,
            @RequestParam(defaultValue = "2") int hops) {
        try {
            GraphQueryResult result = graphService.multiHopReasoning(entity, hops);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("多跳推理失败: " + e.getMessage());
        }
    }

    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportToJson() {
        try {
            byte[] data = graphService.exportToJson();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "graph_data.json");
            headers.setContentLength(data.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportToCsv() {
        try {
            byte[] data = graphService.exportToCsv();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "graph_data.csv");
            headers.setContentLength(data.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/import/json")
    public ApiResponse<String> importFromJson(@RequestParam("file") MultipartFile file) {
        try {
            graphService.importFromJson(file.getBytes());
            return ApiResponse.success("JSON导入成功", null);
        } catch (Exception e) {
            return ApiResponse.error("JSON导入失败: " + e.getMessage());
        }
    }

    @PostMapping("/import/csv")
    public ApiResponse<String> importFromCsv(@RequestParam("file") MultipartFile file) {
        try {
            graphService.importFromCsv(file.getBytes());
            return ApiResponse.success("CSV导入成功", null);
        } catch (Exception e) {
            return ApiResponse.error("CSV导入失败: " + e.getMessage());
        }
    }
}