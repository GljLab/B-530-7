package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("floor")
public class Floor {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long buildingId;

    private Integer floorNumber;

    private String floorName;

    private String features;

    private Integer roomCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private String buildingName;

    @Column(ignore = true)
    private List<Room> rooms;
}
