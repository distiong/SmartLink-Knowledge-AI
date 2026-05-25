package com.ai.knowledge.repository;

import com.ai.knowledge.entity.ChatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRecordRepository extends JpaRepository<ChatRecord, Long> {
    List<ChatRecord> findByUserIdOrderByCreateTimeDesc(Long userId);
    List<ChatRecord> findByUserIdAndChatTypeOrderByCreateTimeDesc(Long userId, String chatType);
    List<ChatRecord> findBySessionIdOrderByCreateTimeAsc(Long sessionId);
    int countBySessionId(Long sessionId);
}
