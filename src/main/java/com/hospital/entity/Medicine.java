package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品实体类
 */
@Data
@TableName("medicine")
public class Medicine {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String medicineName;

    private String medicineCode;

    private String specification; // 规格

    private String manufacturer; // 生产厂家

    private BigDecimal price;

    private Integer stock;

    private String category; // 分类

    private String description;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
