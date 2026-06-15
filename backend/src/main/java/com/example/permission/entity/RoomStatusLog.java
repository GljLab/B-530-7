package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("room_status_log")
public class RoomStatusLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long roomId;

    private String roomNumber;

    private Integer oldStatus;

    private Integer newStatus;

    private String operator;

    private Long operatorId;

    private String remark;

    private LocalDateTime createTime;
}
