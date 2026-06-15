package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据权限实体
 */
@Data
@Table("sys_data_permission")
public class SysDataPermission {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 权限范围类型：1-全部数据，2-自定义数据，3-本部门数据，4-本部门及以下数据，5-仅本人数据
     */
    private Integer scopeType;
    
    /**
     * 自定义部门ID列表（逗号分隔）
     */
    private String customDeptIds;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
