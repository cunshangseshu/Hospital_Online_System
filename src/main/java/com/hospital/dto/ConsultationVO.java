package com.hospital.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问诊详情VO - 返回给前端
 */
@Data
public class ConsultationVO {

    private Long id;

    private String consultationNo;

    private Long patientId;

    private String patientName;

    private Long doctorId;

    private String doctorName;

    private String doctorTitle;

    private Long deptId;

    private String deptName;

    private String type;

    private String typeText; // 文字问诊/视频问诊

    private Integer status;

    private String statusText; // 待接诊/问诊中/已完成/已关闭

    private String symptomDescription;

    private String doctorNote;

    private LocalDateTime startedAt;

    private LocalDateTime createdAt;

    private List<ConsultationMessageVO> messages;

    private List<PrescriptionVO> prescriptions;
}
