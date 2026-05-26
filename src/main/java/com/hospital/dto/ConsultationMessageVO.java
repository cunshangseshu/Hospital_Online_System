package com.hospital.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问诊消息VO
 */
@Data
public class ConsultationMessageVO {

    private Long id;

    private Long senderId;

    private String senderType; // PATIENT, DOCTOR, SYSTEM

    private String senderName;

    private String senderAvatar;

    private String messageType;

    private String content;

    private Long refPrescriptionId;

    private String time;

    private LocalDateTime createdAt;
}
