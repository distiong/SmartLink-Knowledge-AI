package com.ai.knowledge.repository;

import com.ai.knowledge.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByUserIdAndChatTypeOrderByUpdateTimeDesc(Long userId, String chatType);
    List<ChatSession> findByUserIdOrderByUpdateTimeDesc(Long userId);
}
