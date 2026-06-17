package com.hospital.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GenerateScheduleRequest {
    private Long doctorId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer dailySlots;
}
