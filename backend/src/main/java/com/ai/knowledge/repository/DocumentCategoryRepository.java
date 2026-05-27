package com.ai.knowledge.repository;

import com.ai.knowledge.entity.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {

    List<DocumentCategory> findByUserIdOrderBySortAsc(Long userId);

    List<DocumentCategory> findByParentIdOrderBySortAsc(Long parentId);

    List<DocumentCategory> findByUserIdAndParentIdOrderBySortAsc(Long userId, Long parentId);

    List<DocumentCategory> findAllByOrderBySortAsc();

    boolean existsByCategoryNameAndUserId(String categoryName, Long userId);
}
