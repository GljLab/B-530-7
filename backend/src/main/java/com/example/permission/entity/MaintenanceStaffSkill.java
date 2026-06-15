package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("maintenance_staff_skill")
public class MaintenanceStaffSkill {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private String skillType;

    private Integer skillLevel;

    private LocalDateTime createTime;
}
