package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约记录实体类
 */
@Data
@TableName("appointment")
public class Appointment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;

    private Long scheduleId;

    private Long doctorId;

    private Long deptId;

    private LocalDate appointmentDate;

    private String timeSlot;

    private String appointmentNo; // 预约号

    private Integer status; // 0-待确认，1-已确认，2-已完成，3-已取消，4-已过期

    private String symptom; // 症状描述

    private String cancelReason; // 取消原因

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
