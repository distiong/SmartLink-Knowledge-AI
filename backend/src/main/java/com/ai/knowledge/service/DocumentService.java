package com.ai.knowledge.service;

import com.ai.knowledge.entity.Document;
import com.ai.knowledge.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DocumentService {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private MilvusService milvusService;
    
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
    
    public List<Document> getDocumentsByUserId(Long userId) {
        return documentRepository.findByUserId(userId);
    }
    
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
    }
    
    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }
    
    public void deleteDocument(Long id) {
        // 删除Milvus中的向量数据
        milvusService.deleteByDocumentId(id);
        
        // 删除MySQL中的文档记录
        documentRepository.deleteById(id);
    }
}
