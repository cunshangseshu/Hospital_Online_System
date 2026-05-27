package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 开具处方请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {

    private Long consultationId;

    private String diagnosis;

    private String advice;

    private String remark;

    private List<PrescriptionItemRequest> items;
}


