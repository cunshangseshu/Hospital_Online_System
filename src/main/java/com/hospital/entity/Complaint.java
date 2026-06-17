package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("complaint")
public class Complaint {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private String contact;
    private Integer status;
    private String reply;
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;
}
