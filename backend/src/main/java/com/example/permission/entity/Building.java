package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("building")
public class Building {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String buildingName;

    private String buildingCode;

    private Integer totalFloors;

    private String description;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<Floor> floors;
}
