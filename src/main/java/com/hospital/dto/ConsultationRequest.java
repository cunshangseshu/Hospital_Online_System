package com.hospital.dto;

import lombok.Data;

/**
 * 发起问诊请求DTO
 */
@Data
public class ConsultationRequest {

    private Long doctorId;

    private Long deptId;

    private String type; // TEXT, VIDEO

    private String symptomDescription;
}
