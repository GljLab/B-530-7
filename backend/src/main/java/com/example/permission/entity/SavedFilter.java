package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("saved_filter")
public class SavedFilter {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private String filterName;

    private String filterConfig;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
