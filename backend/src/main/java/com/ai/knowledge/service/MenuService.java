package com.ai.knowledge.service;

import com.ai.knowledge.entity.SysMenu;
import com.ai.knowledge.repository.SysMenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class MenuService {
    
    @Autowired
    private SysMenuRepository menuRepository;
    
    public List<SysMenu> getAllMenus() {
        return menuRepository.findAll();
    }
    
    public List<Map<String, Object>> getUserMenus(Long roleId) {
        List<SysMenu> menus = menuRepository.findByRoleId(roleId);
        return buildMenuTree(menus, 0L);
    }
    
    public List<String> getUserPermissions(Long roleId) {
        List<SysMenu> menus = menuRepository.findByRoleId(roleId);
        List<String> permissions = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getMenuPerm() != null && !menu.getMenuPerm().isEmpty()) {
                permissions.add(menu.getMenuPerm());
            }
        }
        return permissions;
    }
    
    public List<Map<String, Object>> getAllMenusAsTree() {
        List<SysMenu> menus = menuRepository.findAll();
        return buildMenuTree(menus, 0L);
    }
    
    public List<Map<String, Object>> getMenusByPermissions(List<String> permCodes) {
        List<SysMenu> allMenus = menuRepository.findAll();
        List<SysMenu> filteredMenus = allMenus.stream()
            .filter(menu -> {
                // 目录类型直接显示
                if (menu.getMenuType() == 1) {
                    return true;
                }
                // 菜单和按钮需要检查权限
                return menu.getMenuPerm() != null && permCodes.contains(menu.getMenuPerm());
            })
            .collect(Collectors.toList());
        
        return buildMenuTree(filteredMenus, 0L);
    }
    
    private List<Map<String, Object>> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId().equals(parentId) && menu.getMenuType() != 3) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", menu.getId());
                node.put("name", menu.getMenuName());
                node.put("path", menu.getMenuPath());
                node.put("component", menu.getMenuComponent());
                node.put("icon", menu.getMenuIcon());
                node.put("perm", menu.getMenuPerm());
                node.put("type", menu.getMenuType());
                node.put("children", buildMenuTree(menus, menu.getId()));
                tree.add(node);
            }
        }
        return tree;
    }
    
    public SysMenu createMenu(SysMenu menu) {
        return menuRepository.save(menu);
    }
    
    public SysMenu updateMenu(Long id, SysMenu updateMenu) {
        SysMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜单不存在"));
        menu.setMenuName(updateMenu.getMenuName());
        menu.setMenuPath(updateMenu.getMenuPath());
        menu.setMenuComponent(updateMenu.getMenuComponent());
        menu.setMenuIcon(updateMenu.getMenuIcon());
        menu.setMenuPerm(updateMenu.getMenuPerm());
        menu.setMenuType(updateMenu.getMenuType());
        menu.setSort(updateMenu.getSort());
        menu.setStatus(updateMenu.getStatus());
        return menuRepository.save(menu);
    }
    
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }
}
