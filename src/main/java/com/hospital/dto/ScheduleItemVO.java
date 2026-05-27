package com.hospital.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 排班列表项视图对象
 */
@Data
public class ScheduleItemVO {
    private LocalDate workDate;
    private String timeSlot;
    private Integer totalSlots;
    private Integer availableSlots;
    private Integer bookedSlots; // 已挂号数
    private Integer status;
}
