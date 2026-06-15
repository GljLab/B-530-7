package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统角色实体
 */
@Data
@Table("sys_role")
public class SysRole {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色标识
     */
    private String roleKey;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer deleted;
    
    /**
     * 角色关联的菜单ID列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<Long> menuIds;
    
    /**
     * 角色关联的菜单列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<SysMenu> menus;
}
