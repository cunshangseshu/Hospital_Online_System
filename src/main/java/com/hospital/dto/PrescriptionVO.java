package com.hospital.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方VO
 */
@Data
public class PrescriptionVO {

    private Long id;

    private String prescriptionNo;

    private Long consultationId;

    private String doctorName;

    private String patientName;

    private String diagnosis;

    private String advice;

    private BigDecimal totalAmount;

    private Integer status;

    private String statusText; // 待审核/已生效/已作废

    private String remark;

    private LocalDateTime createdAt;

    private List<PrescriptionItemVO> items;

    // 前端展示用
    @Data
    public static class PrescriptionItemVO {

        private Long id;

        private Long medicineId;

        private String medicineName;

        private String specification;

        private String dosage;

        private String usageMethod;

        private String frequency;

        private Integer days;

        private Integer quantity;

        private BigDecimal unitPrice;

        private BigDecimal subtotal;

        private String remark;
    }
}
