package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统菜单实体
 */
@Data
@Table("sys_menu")
public class SysMenu {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 菜单名称
     */
    private String menuName;
    
    /**
     * 父菜单ID
     */
    private Long parentId;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
    /**
     * 路由地址
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 权限标识
     */
    private String perms;
    
    /**
     * 菜单类型：0-目录，1-菜单，2-按钮
     */
    private Integer menuType;
    
    /**
     * 是否显示：0-隐藏，1-显示
     */
    private Integer visible;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 菜单图标
     */
    private String icon;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 子菜单列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<SysMenu> children;
}
