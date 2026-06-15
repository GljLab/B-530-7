package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户实体
 */
@Data
@Table("sys_user")
public class SysUser {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
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
     * 用户关联的角色ID列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<Long> roleIds;
    
    /**
     * 用户关联的角色列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<SysRole> roles;
    
    /**
     * 用户权限标识列表（非数据库字段）
     */
    @Column(ignore = true)
    private List<String> permissions;
}
