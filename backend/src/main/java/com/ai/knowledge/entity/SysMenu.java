package com.ai.knowledge.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_menu")
public class SysMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long parentId = 0L;
    
    @Column(nullable = false)
    private String menuName;
    
    private String menuPath;
    
    private String menuComponent;
    
    private String menuIcon;
    
    private String menuPerm;
    
    private Integer menuType = 1;
    
    private Integer sort = 0;
    
    private Integer status = 1;
}
