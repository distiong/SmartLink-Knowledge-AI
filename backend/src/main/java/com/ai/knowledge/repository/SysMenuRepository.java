package com.ai.knowledge.repository;

import com.ai.knowledge.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {
    @Query("SELECT m FROM SysMenu m WHERE m.id IN (SELECT rm.menuId FROM SysRoleMenu rm WHERE rm.roleId = ?1) ORDER BY m.sort")
    List<SysMenu> findByRoleId(Long roleId);
    
    @Query("SELECT m FROM SysMenu m WHERE m.menuType = 3 AND m.id IN (SELECT rm.menuId FROM SysRoleMenu rm WHERE rm.roleId = ?1)")
    List<SysMenu> findButtonsByRoleId(Long roleId);
}
