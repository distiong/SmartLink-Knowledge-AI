package com.ai.knowledge.service;

import com.ai.knowledge.dto.GraphRelation;
import com.ai.knowledge.dto.GraphQueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GraphService {

    private final Map<String, List<Map<String, String>>> graphData = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void createRelation(GraphRelation relation) {
        String subject = relation.getSubject();
        String rel = relation.getRelation();
        String object = relation.getObject();

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

                Map<String, Object> edge = new HashMap<>();
                edge.put("id", (subject + relation + object).hashCode());
                edge.put("source", subject.hashCode());
                edge.put("target", object.hashCode());
                edge.put("label", relation);
                edges.add(edge);

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

    public GraphQueryResult multiHopReasoning(String entityName, int hops) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        List<Map<String, Object>> relations = new ArrayList<>();

        Set<String> nodeIds = new HashSet<>();
        Set<String> visitedEntities = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        queue.add(entityName);
        visitedEntities.add(entityName);
        int currentHop = 0;

        while (!queue.isEmpty() && currentHop < hops) {
            int levelSize = queue.size();
            Set<String> nextLevelEntities = new HashSet<>();

            for (int i = 0; i < levelSize; i++) {
                String currentEntity = queue.poll();

                List<Map<String, String>> allRelations = graphData.getOrDefault("relations", new ArrayList<>());

                for (Map<String, String> rel : allRelations) {
                    String subject = rel.get("subject");
                    String object = rel.get("object");
                    String relation = rel.get("relation");

                    if (currentEntity.equals(subject) || currentEntity.equals(object)) {
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

                        Map<String, Object> edge = new HashMap<>();
                        edge.put("id", (subject + relation + object).hashCode());
                        edge.put("source", subject.hashCode());
                        edge.put("target", object.hashCode());
                        edge.put("label", relation);
                        edges.add(edge);

                        Map<String, Object> relationMap = new HashMap<>();
                        relationMap.put("subject", subject);
                        relationMap.put("relation", relation);
                        relationMap.put("object", object);
                        relations.add(relationMap);

                        String nextEntity = currentEntity.equals(subject) ? object : subject;
                        if (!visitedEntities.contains(nextEntity)) {
                            nextLevelEntities.add(nextEntity);
                            visitedEntities.add(nextEntity);
                        }
                    }
                }
            }

            queue.addAll(nextLevelEntities);
            currentHop++;
        }

        return new GraphQueryResult(nodes, edges, relations);
    }

    public byte[] exportToJson() throws Exception {
        List<Map<String, String>> allRelations = graphData.getOrDefault("relations", new ArrayList<>());
        return objectMapper.writeValueAsBytes(allRelations);
    }

    public byte[] exportToCsv() throws Exception {
        List<Map<String, String>> allRelations = graphData.getOrDefault("relations", new ArrayList<>());

        StringBuilder csv = new StringBuilder();
        csv.append("subject,relation,object\n");

        for (Map<String, String> rel : allRelations) {
            csv.append(escapeCsv(rel.get("subject"))).append(",");
            csv.append(escapeCsv(rel.get("relation"))).append(",");
            csv.append(escapeCsv(rel.get("object"))).append("\n");
        }

        return csv.toString().getBytes("UTF-8");
    }

    public void importFromJson(byte[] data) throws Exception {
        List<Map<String, String>> imported = objectMapper.readValue(data, List.class);
        List<Map<String, String>> relations = graphData.computeIfAbsent("relations", k -> new ArrayList<>());
        relations.addAll(imported);
    }

    public void importFromCsv(byte[] data) throws Exception {
        String content = new String(data, "UTF-8");
        String[] lines = content.split("\n");

        List<Map<String, String>> relations = graphData.computeIfAbsent("relations", k -> new ArrayList<>());

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] parts = parseCsvLine(line);
            if (parts.length >= 3) {
                Map<String, String> relation = new HashMap<>();
                relation.put("subject", parts[0]);
                relation.put("relation", parts[1]);
                relation.put("object", parts[2]);
                relations.add(relation);
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());

        return result.toArray(new String[0]);
    }
}
