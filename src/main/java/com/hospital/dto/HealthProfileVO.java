package com.hospital.dto;

import lombok.Data;
import java.util.List;

@Data
public class HealthProfileVO {
    private String patientName;
    private String phone;
    private String email;
    private Integer gender;
    private String genderText;
    private int appointmentCount;
    private int consultationCount;
    private int prescriptionCount;
    private List<AppointmentListItemVO> recentAppointments;
    private List<ConsultationVO> recentConsultations;
    private List<PrescriptionVO> recentPrescriptions;
}
