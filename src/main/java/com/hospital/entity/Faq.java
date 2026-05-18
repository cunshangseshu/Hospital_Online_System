package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("faq")
public class Faq {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String category;
    private String question;
    private String answer;
    private Integer sortOrder;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
