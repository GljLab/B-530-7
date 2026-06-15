package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("room")
public class Room {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String roomNumber;

    private Long buildingId;

    private Long floorId;

    private Long roomTypeId;

    private String orientation;

    private String viewType;

    private String locationFeatures;

    private String specialTags;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private String buildingName;

    @Column(ignore = true)
    private String floorName;

    @Column(ignore = true)
    private Integer floorNumber;

    @Column(ignore = true)
    private String roomTypeName;

    @Column(ignore = true)
    private RoomType roomType;
}
