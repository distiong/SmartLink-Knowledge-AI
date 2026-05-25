package com.ai.knowledge.controller;

import com.ai.knowledge.dto.ApiResponse;
import com.ai.knowledge.entity.SysMenu;
import com.ai.knowledge.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuController {
    
    @Autowired
    private MenuService menuService;
    
    @GetMapping("/list")
    public ApiResponse<List<SysMenu>> getMenuList() {
        try {
            return ApiResponse.success(menuService.getAllMenus());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<SysMenu> createMenu(@RequestBody SysMenu menu) {
        try {
            return ApiResponse.success(menuService.createMenu(menu));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<SysMenu> updateMenu(@PathVariable Long id, @RequestBody SysMenu menu) {
        try {
            return ApiResponse.success(menuService.updateMenu(id, menu));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMenu(@PathVariable Long id) {
        try {
            menuService.deleteMenu(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
