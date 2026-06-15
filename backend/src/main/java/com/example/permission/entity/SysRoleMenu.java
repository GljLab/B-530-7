package com.example.permission.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 角色菜单关联表
 */
@Data
@Table("sys_role_menu")
public class SysRoleMenu {
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 菜单ID
     */
    private Long menuId;
}
