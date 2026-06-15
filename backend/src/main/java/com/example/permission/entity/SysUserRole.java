package com.example.permission.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 用户角色关联表
 */
@Data
@Table("sys_user_role")
public class SysUserRole {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色ID
     */
    private Long roleId;
}
