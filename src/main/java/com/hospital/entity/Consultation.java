package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 在线问诊记录实体类
 */
@Data
@TableName("consultation")
public class Consultation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String consultationNo;

    private Long patientId;

    private Long doctorId;

    private Long deptId;

    private String type; // TEXT, VIDEO

    private Integer status; // 0-待接诊, 1-问诊中, 2-已完成, 3-已关闭

    private String symptomDescription;

    private String doctorNote;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
