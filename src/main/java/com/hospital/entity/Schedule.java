package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班实体类
 */
@Data
@TableName("schedule")
public class Schedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long doctorId;

    private LocalDate workDate;

    private String timeSlot; // MORNING, AFTERNOON, EVENING

    private Integer totalSlots; // 总号源数

    private Integer availableSlots; // 剩余号源数

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    /**
     * 时间段文本（非数据库字段，仅用于前端展示）
     */
    @TableField(exist = false)
    private String timeSlotText;

    /**
     * 获取时间段的中文文本
     */
    public String getTimeSlotText() {
        if (timeSlotText != null) {
            return timeSlotText;
        }
        if (timeSlot == null) {
            return "";
        }
        switch (timeSlot) {
            case "MORNING":
                return "上午";
            case "AFTERNOON":
                return "下午";
            case "EVENING":
                return "晚上";
            default:
                return timeSlot;
        }
    }
}
