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
        milvusService.deleteByDocumentId(id);
        documentRepository.deleteById(id);
    }

    public List<Document> getDocumentsByCategory(Long categoryId) {
        return documentRepository.findByCategoryId(categoryId);
    }

    public List<Document> getDocumentsByUserIdAndCategory(Long userId, Long categoryId) {
        return documentRepository.findByUserIdAndCategoryId(userId, categoryId);
    }

    public List<Document> getDocumentsByTag(Long userId, String tag) {
        if (userId != null) {
            return documentRepository.findByUserIdAndTag(userId, tag);
        }
        return documentRepository.findByTag(tag);
    }

    public List<Document> searchDocumentsByName(String fileName) {
        return documentRepository.findByFileNameContaining(fileName);
    }
}
