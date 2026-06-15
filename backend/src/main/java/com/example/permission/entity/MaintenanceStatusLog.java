package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("maintenance_status_log")
public class MaintenanceStatusLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long orderId;

    private String orderNo;

    private Integer oldStatus;

    private Integer newStatus;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime createTime;

    @com.mybatisflex.annotation.Column(ignore = true)
    private String note;

    @com.mybatisflex.annotation.Column(ignore = true)
    private java.util.List<String> photos;
}
