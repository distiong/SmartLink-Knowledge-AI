package com.ai.knowledge.service;

import com.ai.knowledge.entity.SysRole;
import com.ai.knowledge.entity.SysRoleMenu;
import com.ai.knowledge.repository.SysRoleRepository;
import com.ai.knowledge.repository.SysRoleMenuRepository;
import com.ai.knowledge.repository.SysMenuRepository;
import com.ai.knowledge.entity.SysMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class RoleService {
    
    @Autowired
    private SysRoleRepository roleRepository;
    
    @Autowired
    private SysRoleMenuRepository roleMenuRepository;
    
    @Autowired
    private SysMenuRepository menuRepository;
    
    public List<SysRole> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public SysRole getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
    }
    
    public SysRole createRole(SysRole role) {
        return roleRepository.save(role);
    }
    
    public SysRole updateRole(Long id, SysRole updateRole) {
        SysRole role = getRoleById(id);
        role.setRoleName(updateRole.getRoleName());
        role.setDescription(updateRole.getDescription());
        return roleRepository.save(role);
    }
    
    public void deleteRole(Long id) {
        roleMenuRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
    }
    
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuRepository.deleteByRoleId(roleId);
        
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuRepository.save(roleMenu);
        }
    }
    
    public Map<String, Object> getRoleWithMenus(Long roleId) {
        SysRole role = getRoleById(roleId);
        List<SysMenu> menus = menuRepository.findByRoleId(roleId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("role", role);
        result.put("menus", menus);
        
        return result;
    }
    
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMenuRepository.findByRoleId(roleId)
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }
}
