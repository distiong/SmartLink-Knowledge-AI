package com.ai.knowledge.service;

import com.ai.knowledge.dto.ChatResponse;
import com.ai.knowledge.dto.SearchResult;
import com.ai.knowledge.entity.Document;
import com.ai.knowledge.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentParserService documentParserService;

    @Autowired
    private MilvusService milvusService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private AiService aiService;

    public Document uploadDocument(MultipartFile file, Long userId) throws IOException {
        return uploadDocument(file, userId, null, null);
    }

    public Document uploadDocument(MultipartFile file, Long userId, Long categoryId, String tags) throws IOException {
        String content = documentParserService.parseDocument(file);

        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setFileType(getFileType(file.getOriginalFilename()));
        document.setContent(content);
        document.setUserId(userId);
        document.setCategoryId(categoryId);
        document.setTags(tags);
        document.setVersion(1);
        document = documentRepository.save(document);

        List<String> chunks = splitText(content);

        List<List<Float>> vectors = embeddingService.batchEmbed(chunks);

        milvusService.insertVectors(document.getId(), chunks, vectors);

        return document;
    }

    public List<SearchResult> search(String query, int topK) {
        List<Float> queryVector = embeddingService.embed(query);

        String vectorStr = queryVector.toString();
        return milvusService.search(vectorStr, topK);
    }

    public List<SearchResult> search(String query, int topK, Long userId) {
        List<Float> queryVector = embeddingService.embed(query);

        String vectorStr = queryVector.toString();
        return milvusService.search(vectorStr, topK, userId);
    }

    public ChatResponse ragChatWithContext(String question, Long userId, List<Map<String, String>> history) {
        try {
            List<Float> queryVector = embeddingService.embed(question);

            List<SearchResult> searchResults;
            if (userId != null) {
                searchResults = milvusService.search(queryVector.toString(), 5, userId);
            } else {
                searchResults = milvusService.search(queryVector.toString(), 5);
            }

            String context = searchResults.stream()
                    .map(SearchResult::getText)
                    .collect(Collectors.joining("\n\n"));

            StringBuilder historyContext = new StringBuilder();
            if (history != null && !history.isEmpty()) {
                historyContext.append("\n\n历史对话：\n");
                for (Map<String, String> msg : history) {
                    String role = "user".equals(msg.get("role")) ? "用户" : "AI";
                    historyContext.append(role).append(": ").append(msg.get("content")).append("\n");
                }
            }

            String ragPrompt = """
                    基于以下参考资料和历史对话上下文回答用户问题。如果参考资料中没有相关信息，请说明无法回答。请结合历史对话理解用户的追问意图。

                    参考资料：
                    %s
                    %s

                    用户问题：%s

                    请基于参考资料和历史对话给出准确回答，并在回答末尾标注引用的参考资料片段。
                    """.formatted(context, historyContext.toString(), question);

            String answer = aiService.chat(ragPrompt, 0.7);

            return new ChatResponse(answer, context);
        } catch (Exception e) {
            throw new RuntimeException("RAG对话失败: " + e.getMessage(), e);
        }
    }

    private List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();
        int chunkSize = 500;
        int overlap = 50;

        for (int i = 0; i < text.length(); i += chunkSize - overlap) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end));
            if (end == text.length()) break;
        }

        return chunks;
    }

    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
