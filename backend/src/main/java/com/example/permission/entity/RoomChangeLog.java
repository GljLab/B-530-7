package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("room_change_log")
public class RoomChangeLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long roomId;

    private String roomNumber;

    private String operator;

    private Long operatorId;

    private String operatorName;

    private String operatorRole;

    private Integer operationType;

    private String changeField;

    private String oldValue;

    private String newValue;

    private String changeReason;

    private String relatedOrderNo;

    private Long relatedOrderId;

    private String terminalType;

    private String terminalIp;

    private LocalDateTime createTime;
}
