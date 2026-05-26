package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 处方明细实体类
 */
@Data
@TableName("prescription_item")
public class PrescriptionItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long prescriptionId;

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
