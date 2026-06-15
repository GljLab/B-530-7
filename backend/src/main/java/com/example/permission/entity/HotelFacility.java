package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("hotel_facility")
public class HotelFacility {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long hotelId;

    private String facilityName;

    private String facilityIcon;

    private String openTime;

    private String description;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
