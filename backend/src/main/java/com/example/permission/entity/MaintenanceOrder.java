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
@Table("maintenance_order")
public class MaintenanceOrder {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String orderNo;

    private Long roomId;

    private String roomNumber;

    private Integer maintenanceType;

    private Integer priority;

    private String problemDescription;

    private LocalDateTime expectedFinishTime;

    private String specialRemark;

    private Integer status;

    private Long assignedUserId;

    private String assignedUserName;

    private LocalDateTime assignTime;

    private LocalDateTime acceptTime;

    private BigDecimal actualHours;

    private String usedParts;

    private BigDecimal maintenanceCost;

    private String maintenanceDescription;

    private LocalDateTime finishTime;

    private Long inspectorId;

    private String inspectorName;

    private LocalDateTime inspectTime;

    private Integer inspectResult;

    private String inspectOpinion;

    private String rectificationRequirement;

    private Long createUserId;

    private String createUserName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<MaintenancePhoto> problemPhotos;

    @Column(ignore = true)
    private List<MaintenancePhoto> afterPhotos;

    @Column(ignore = true)
    private List<MaintenanceStatusLog> statusLogs;

    @Column(ignore = true)
    private String maintenanceTypeName;

    @Column(ignore = true)
    private String creatorName;

    @Column(ignore = true)
    private List<MaintenanceStatusLog> progressList;
}
