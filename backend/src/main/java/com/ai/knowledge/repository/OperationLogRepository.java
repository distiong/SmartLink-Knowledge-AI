package com.ai.knowledge.repository;

import com.ai.knowledge.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    Page<OperationLog> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    Page<OperationLog> findAllByOrderByCreateTimeDesc(Pageable pageable);

    @Query("SELECT l FROM OperationLog l WHERE (:userId IS NULL OR l.userId = :userId) AND (:operationType IS NULL OR l.operationType = :operationType) AND (:startTime IS NULL OR l.createTime >= :startTime) AND (:endTime IS NULL OR l.createTime <= :endTime) ORDER BY l.createTime DESC")
    Page<OperationLog> findByConditions(@Param("userId") Long userId, @Param("operationType") String operationType, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);

    List<OperationLog> findByOperationType(String operationType);

    long countByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
}
