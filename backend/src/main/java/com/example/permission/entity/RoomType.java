package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("room_type")
public class RoomType {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String typeName;

    private String typeCode;

    private BigDecimal area;

    private String bedType;

    private Integer maxOccupancy;

    private String extraBedPolicy;

    private String facilities;

    private BigDecimal basePrice;

    private BigDecimal weekendPrice;

    private BigDecimal costPrice;

    private Integer saleStatus;

    private String description;

    private Integer roomCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<HotelImage> images;
}
