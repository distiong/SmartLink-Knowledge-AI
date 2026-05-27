package com.ai.knowledge.service;

import com.ai.knowledge.entity.DocumentCategory;
import com.ai.knowledge.repository.DocumentCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DocumentCategoryService {

    @Autowired
    private DocumentCategoryRepository categoryRepository;

    public List<DocumentCategory> getAllCategories() {
        return categoryRepository.findAllByOrderBySortAsc();
    }

    public List<DocumentCategory> getCategoriesByUserId(Long userId) {
        return categoryRepository.findByUserIdOrderBySortAsc(userId);
    }

    public List<DocumentCategory> getRootCategories(Long userId) {
        return categoryRepository.findByUserIdAndParentIdOrderBySortAsc(userId, 0L);
    }

    public List<DocumentCategory> getChildCategories(Long parentId) {
        return categoryRepository.findByParentIdOrderBySortAsc(parentId);
    }

    public DocumentCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
    }

    public DocumentCategory createCategory(DocumentCategory category) {
        if (categoryRepository.existsByCategoryNameAndUserId(category.getCategoryName(), category.getUserId())) {
            throw new RuntimeException("该分类名称已存在");
        }
        return categoryRepository.save(category);
    }

    public DocumentCategory updateCategory(Long id, DocumentCategory category) {
        DocumentCategory existing = getCategoryById(id);
        existing.setCategoryName(category.getCategoryName());
        existing.setParentId(category.getParentId());
        existing.setSort(category.getSort());
        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
