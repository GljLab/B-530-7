package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("hotel_info")
public class HotelInfo {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String hotelName;

    private String brand;

    private Integer starRating;

    private String address;

    private String contactPhone;

    private String licenseNumber;

    private LocalDate openingDate;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<HotelFacility> facilities;

    @Column(ignore = true)
    private List<HotelImage> images;
}
