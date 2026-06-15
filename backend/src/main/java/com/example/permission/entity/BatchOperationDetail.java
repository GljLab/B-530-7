package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("batch_operation_detail")
public class BatchOperationDetail {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long batchId;

    private String batchNo;

    private Integer targetType;

    private Long targetId;

    private String targetNo;

    private Integer resultStatus;

    private String failReason;

    private String oldValue;

    private String newValue;

    private LocalDateTime createTime;
}
