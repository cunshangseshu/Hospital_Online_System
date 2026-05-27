package com.hospital.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 预约请求DTO
 */
@Data
public class AppointmentRequest {

    private Long doctorId;

    private Long scheduleId;

    private LocalDate appointmentDate;

    private String timeSlot;

    private String symptom; // 症状描述
}
