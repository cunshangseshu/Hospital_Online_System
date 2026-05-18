package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItemRequest {

    private Long medicineId;

    private String dosage;

    private String usageMethod;

    private String frequency;

    private Integer days;

    private Integer quantity;
}