package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("hotel_image")
public class HotelImage {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String refType;

    private Long refId;

    private String imageUrl;

    private Integer isMain;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
