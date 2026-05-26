package com.hospital.dto;

import lombok.Data;

/**
 * 改签预约请求DTO
 */
@Data
public class RescheduleRequest {

    private Long newScheduleId;

    private String newAppointmentDate;

    private String newTimeSlot;
}
