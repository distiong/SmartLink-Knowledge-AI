package com.ai.knowledge.service;

import com.ai.knowledge.dto.GraphRelation;
import com.ai.knowledge.dto.GraphQueryResult;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GraphService {
    
    // 内存存储 - 使用普通HashMap和ArrayList
    private final Map<String, List<Map<String, String>>> graphData = new HashMap<>();
    
    public void createRelation(GraphRelation relation) {
        String subject = relation.getSubject();
        String rel = relation.getRelation();
        String object = relation.getObject();
        
        // 存储关系
        String key = subject + "->" + rel + "->" + object;
        List<Map<String, String>> relations = graphData.computeIfAbsent("relations", k -> new ArrayList<>());
        
        Map<String, String> relationMap = new HashMap<>();
        relationMap.put("subject", subject);
        relationMap.put("relation", rel);
        relationMap.put("object", object);
        relations.add(relationMap);
        
        System.out.println("Created relation: " + key);
        System.out.println("Total relations: " + relations.size());
    }
    
    public GraphQueryResult queryRelations(String entityName) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        List<Map<String, Object>> relations = new ArrayList<>();
        
        Set<String> nodeIds = new HashSet<>();
        
        List<Map<String, String>> allRelations = graphData.getOrDefault("relations", new ArrayList<>());
        
        System.out.println("Querying entity: " + entityName);
        System.out.println("Total relations in store: " + allRelations.size());
        
        for (Map<String, String> rel : allRelations) {
            String subject = rel.get("subject");
            String object = rel.get("object");
            String relation = rel.get("relation");
            
            System.out.println("Checking relation: " + subject + " -> " + relation + " -> " + object);
            
            if (entityName.equals(subject) || entityName.equals(object)) {
                // 添加节点
                if (!nodeIds.contains(subject)) {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", subject.hashCode());
                    node.put("name", subject);
                    nodes.add(node);
                    nodeIds.add(subject);
                }
                
                if (!nodeIds.contains(object)) {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", object.hashCode());
                    node.put("name", object);
                    nodes.add(node);
                    nodeIds.add(object);
                }
                
                // 添加边
                Map<String, Object> edge = new HashMap<>();
                edge.put("id", (subject + relation + object).hashCode());
                edge.put("source", subject.hashCode());
                edge.put("target", object.hashCode());
                edge.put("label", relation);
                edges.add(edge);
                
                // 添加关系
                Map<String, Object> relationMap = new HashMap<>();
                relationMap.put("subject", subject);
                relationMap.put("relation", relation);
                relationMap.put("object", object);
                relations.add(relationMap);
            }
        }
        
        System.out.println("Found nodes: " + nodes.size());
        System.out.println("Found edges: " + edges.size());
        System.out.println("Found relations: " + relations.size());
        
        return new GraphQueryResult(nodes, edges, relations);
    }
}
