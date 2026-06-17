package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 处方实体类
 */
@Data
@TableName("prescription")
public class Prescription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String prescriptionNo;

    private Long consultationId;

    private Long doctorId;

    private Long patientId;

    private String diagnosis;

    private String advice;

    private BigDecimal totalAmount;

    private Integer status; // 0-待审核, 1-已生效, 2-已作废

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
