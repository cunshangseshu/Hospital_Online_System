package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问诊消息实体类
 */
@Data
@TableName("consultation_message")
public class ConsultationMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long consultationId;

    private Long senderId;

    private String senderType; // PATIENT, DOCTOR, SYSTEM

    private String messageType; // TEXT, IMAGE, PRESCRIPTION

    private String content;

    private Long refPrescriptionId;

    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
