package com.ai.knowledge.service;

import com.ai.knowledge.dto.SearchResult;
import com.ai.knowledge.entity.Document;
import com.ai.knowledge.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    
    public Document uploadDocument(MultipartFile file, Long userId) throws IOException {
        String content = documentParserService.parseDocument(file);
        
        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setFileType(getFileType(file.getOriginalFilename()));
        document.setContent(content);
        document.setUserId(userId);
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
