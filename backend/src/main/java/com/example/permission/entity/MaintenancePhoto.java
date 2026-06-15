package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("maintenance_photo")
public class MaintenancePhoto {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long orderId;

    private Integer photoType;

    private String photoUrl;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
