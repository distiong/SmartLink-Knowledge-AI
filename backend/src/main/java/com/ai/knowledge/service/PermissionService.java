package com.ai.knowledge.service;

import com.ai.knowledge.entity.SysUserPermission;
import com.ai.knowledge.repository.SysUserPermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class PermissionService {
    
    @Autowired
    private SysUserPermissionRepository userPermissionRepository;
    
    public List<SysUserPermission> getUserPermissions(Long userId) {
        return userPermissionRepository.findByUserId(userId);
    }
    
    public List<String> getUserPermCodes(Long userId) {
        return userPermissionRepository.findPermCodesByUserId(userId);
    }
    
    public boolean hasPermission(Long userId, String permCode) {
        return userPermissionRepository.existsByUserIdAndPermCode(userId, permCode);
    }
    
    @Transactional
    public void assignPermissions(Long userId, List<Map<String, Object>> permissions) {
        // 删除用户原有权限
        userPermissionRepository.deleteByUserId(userId);
        
        // 添加新权限
        List<SysUserPermission> entities = new ArrayList<>();
        for (Map<String, Object> perm : permissions) {
            SysUserPermission entity = new SysUserPermission();
            entity.setUserId(userId);
            entity.setPermCode((String) perm.get("permCode"));
            entity.setPermName((String) perm.get("permName"));
            entity.setPermType((String) perm.get("permType"));
            entity.setStatus(1);
            entities.add(entity);
        }
        
        userPermissionRepository.saveAll(entities);
    }
    
    @Transactional
    public void assignDefaultPermissions(Long userId, String roleCode) {
        List<Map<String, Object>> permissions = new ArrayList<>();
        
        // 根据角色分配默认权限
        switch (roleCode) {
            case "admin":
                addPermission(permissions, "ai:chat", "AI通用对话", "menu");
                addPermission(permissions, "ai:chat:history", "查看对话记录", "button");
                addPermission(permissions, "rag:manage", "知识库管理", "menu");
                addPermission(permissions, "rag:chat", "RAG问答", "menu");
                addPermission(permissions, "rag:document", "文档管理", "menu");
                addPermission(permissions, "rag:document:upload", "文档上传", "button");
                addPermission(permissions, "rag:document:delete", "文档删除", "button");
                addPermission(permissions, "search:query", "向量检索", "menu");
                addPermission(permissions, "graph:manage", "知识图谱", "menu");
                addPermission(permissions, "graph:create", "图谱创建", "button");
                addPermission(permissions, "graph:query", "图谱查询", "button");
                addPermission(permissions, "system:manage", "系统管理", "menu");
                addPermission(permissions, "system:user", "用户管理", "menu");
                addPermission(permissions, "system:user:add", "用户新增", "button");
                addPermission(permissions, "system:user:edit", "用户编辑", "button");
                addPermission(permissions, "system:user:delete", "用户删除", "button");
                addPermission(permissions, "system:user:permission", "权限配置", "button");
                addPermission(permissions, "system:role", "角色管理", "menu");
                addPermission(permissions, "data:all", "查看全量数据", "function");
                break;
            case "operator":
                addPermission(permissions, "ai:chat", "AI通用对话", "menu");
                addPermission(permissions, "ai:chat:history", "查看对话记录", "button");
                addPermission(permissions, "rag:manage", "知识库管理", "menu");
                addPermission(permissions, "rag:chat", "RAG问答", "menu");
                addPermission(permissions, "rag:document", "文档管理", "menu");
                addPermission(permissions, "rag:document:upload", "文档上传", "button");
                addPermission(permissions, "search:query", "向量检索", "menu");
                break;
            case "visitor":
                addPermission(permissions, "ai:chat", "AI通用对话", "menu");
                break;
            default:
                addPermission(permissions, "ai:chat", "AI通用对话", "menu");
                break;
        }
        
        assignPermissions(userId, permissions);
    }
    
    private void addPermission(List<Map<String, Object>> permissions, String code, String name, String type) {
        Map<String, Object> perm = new HashMap<>();
        perm.put("permCode", code);
        perm.put("permName", name);
        perm.put("permType", type);
        permissions.add(perm);
    }
    
    public Map<String, Object> getAllPermissionItems() {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> menuPerms = new ArrayList<>();
        addPermItem(menuPerms, "ai:chat", "AI通用对话", "menu");
        addPermItem(menuPerms, "rag:manage", "知识库管理", "menu");
        addPermItem(menuPerms, "rag:chat", "RAG问答", "menu");
        addPermItem(menuPerms, "rag:document", "文档管理", "menu");
        addPermItem(menuPerms, "search:query", "向量检索", "menu");
        addPermItem(menuPerms, "graph:manage", "知识图谱", "menu");
        addPermItem(menuPerms, "system:manage", "系统管理", "menu");
        addPermItem(menuPerms, "system:user", "用户管理", "menu");
        addPermItem(menuPerms, "system:role", "角色管理", "menu");
        
        List<Map<String, Object>> buttonPerms = new ArrayList<>();
        addPermItem(buttonPerms, "ai:chat:history", "查看对话记录", "button");
        addPermItem(buttonPerms, "rag:document:upload", "文档上传", "button");
        addPermItem(buttonPerms, "rag:document:delete", "文档删除", "button");
        addPermItem(buttonPerms, "graph:create", "图谱创建", "button");
        addPermItem(buttonPerms, "graph:query", "图谱查询", "button");
        addPermItem(buttonPerms, "system:user:add", "用户新增", "button");
        addPermItem(buttonPerms, "system:user:edit", "用户编辑", "button");
        addPermItem(buttonPerms, "system:user:delete", "用户删除", "button");
        addPermItem(buttonPerms, "system:user:permission", "权限配置", "button");
        addPermItem(buttonPerms, "system:role:add", "角色新增", "button");
        addPermItem(buttonPerms, "system:role:edit", "角色编辑", "button");
        addPermItem(buttonPerms, "system:role:delete", "角色删除", "button");
        
        List<Map<String, Object>> funcPerms = new ArrayList<>();
        addPermItem(funcPerms, "data:all", "查看全量数据", "function");
        
        result.put("menuPermissions", menuPerms);
        result.put("buttonPermissions", buttonPerms);
        result.put("functionPermissions", funcPerms);
        
        return result;
    }
    
    private void addPermItem(List<Map<String, Object>> list, String code, String name, String type) {
        Map<String, Object> item = new HashMap<>();
        item.put("code", code);
        item.put("name", name);
        item.put("type", type);
        list.add(item);
    }
}
