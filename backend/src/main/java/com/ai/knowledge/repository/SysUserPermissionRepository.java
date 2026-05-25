package com.ai.knowledge.repository;

import com.ai.knowledge.entity.SysUserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface SysUserPermissionRepository extends JpaRepository<SysUserPermission, Long> {
    List<SysUserPermission> findByUserId(Long userId);
    
    List<SysUserPermission> findByUserIdAndStatus(Long userId, Integer status);
    
    @Query("SELECT p.permCode FROM SysUserPermission p WHERE p.userId = ?1 AND p.status = 1")
    List<String> findPermCodesByUserId(Long userId);
    
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
    
    boolean existsByUserIdAndPermCode(Long userId, String permCode);
}
