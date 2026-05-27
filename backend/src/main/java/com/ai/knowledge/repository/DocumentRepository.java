package com.ai.knowledge.repository;

import com.ai.knowledge.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserId(Long userId);

    List<Document> findByCategoryId(Long categoryId);

    List<Document> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.tags LIKE %:tag%")
    List<Document> findByUserIdAndTag(@Param("userId") Long userId, @Param("tag") String tag);

    @Query("SELECT d FROM Document d WHERE d.tags LIKE %:tag%")
    List<Document> findByTag(@Param("tag") String tag);

    List<Document> findByFileNameContaining(String fileName);
}
