package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("maintenance_assign_rule")
public class MaintenanceAssignRule {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String ruleName;

    private Integer ruleType;

    private String conditionValue;

    private Long targetUserId;

    private String targetRoleKey;

    private Integer priority;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
