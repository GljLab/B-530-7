package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("batch_operation")
public class BatchOperation {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String batchNo;

    private Integer operationType;

    private Integer targetStatus;

    private String attrType;

    private Integer attrMode;

    private String attrValue;

    private String operationReason;

    private Long operatorId;

    private String operatorName;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private Integer skipCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime finishTime;
}
