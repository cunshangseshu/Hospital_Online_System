package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.dto.AppointmentRequest;
import com.hospital.entity.Appointment;
import java.time.LocalDate;
import java.util.List;

/**
 * 预约服务接口
 */
public interface AppointmentService extends IService<Appointment> {

    /**
     * 创建预约
     */
    Appointment createAppointment(AppointmentRequest request);

    /**
     * 取消预约
     */
    boolean cancelAppointment(Long appointmentId, String reason);

    /**
     * 查询患者的预约列表
     */
    List<Appointment> getByPatientId(Long patientId);

    /**
     * 查询医生的预约列表
     */
    List<Appointment> getByDoctorId(Long doctorId, LocalDate date);

    /**
     * 根据预约号查询
     */
    Appointment getByAppointmentNo(String appointmentNo);
}
