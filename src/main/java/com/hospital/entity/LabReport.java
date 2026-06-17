package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("lab_report")
public class LabReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportNo;
    private Long patientId;
    private Long doctorId;
    private String reportName;
    private String category;
    private String sampleType;
    private String result;
    private String conclusion;
    private Integer normalFlag;
    private LocalDate reportDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
