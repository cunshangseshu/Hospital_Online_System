package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生信息实体类
 */
@Data
@TableName("doctor")
public class Doctor {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long deptId;

    private String title; // 职称

    private String specialty; // 擅长领域

    private String education; // 学历

    private Integer experience; // 从业年限

    private String introduction; // 简介

    private BigDecimal consultationFee; // 挂号费

    private BigDecimal rating; // 评分

    private Integer totalAppointments; // 累计接诊数

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
