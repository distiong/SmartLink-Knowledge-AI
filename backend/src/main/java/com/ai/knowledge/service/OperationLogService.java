package com.ai.knowledge.service;

import com.ai.knowledge.entity.OperationLog;
import com.ai.knowledge.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationLogService {

    @Autowired
    private OperationLogRepository logRepository;

    public OperationLog saveLog(OperationLog log) {
        return logRepository.save(log);
    }

    public void recordLog(Long userId, String username, String operation, String operationType, String ipAddress) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(operation);
        log.setOperationType(operationType);
        log.setIpAddress(ipAddress);
        logRepository.save(log);
    }

    public Page<OperationLog> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return logRepository.findAllByOrderByCreateTimeDesc(pageable);
    }

    public Page<OperationLog> getLogsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return logRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
    }

    public Page<OperationLog> getLogsByConditions(Long userId, String operationType, LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return logRepository.findByConditions(userId, operationType, startTime, endTime, pageable);
    }

    public long countTodayLogs() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = LocalDateTime.now();
        return logRepository.countByCreateTimeBetween(start, end);
    }
}
