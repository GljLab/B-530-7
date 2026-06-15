package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("user_floor_permission")
public class UserFloorPermission {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private Long buildingId;

    private Long floorId;

    private LocalDateTime createTime;
}
