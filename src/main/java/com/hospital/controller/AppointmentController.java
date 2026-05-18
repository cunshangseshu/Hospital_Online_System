package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.AppointmentRequest;
import com.hospital.entity.Appointment;
import com.hospital.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约控制器 - 处理预约相关AJAX请求
 */
@RestController
@RequestMapping("/api/appointments")
@CrossOrigin
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 创建预约（AJAX异步提交）
     */
    @PostMapping("/create")
    public Result<Appointment> create(@RequestBody AppointmentRequest request) {
        try {
            Appointment appointment = appointmentService.createAppointment(request);
            return Result.success("预约成功", appointment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消预约（AJAX异步提交）
     */
    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id, @RequestParam String reason) {
        try {
            appointmentService.cancelAppointment(id, reason);
            return Result.success("取消成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询患者的预约列表（AJAX动态加载）
     */
    @GetMapping("/my-appointments")
    public Result<List<Appointment>> getMyAppointments(@RequestParam Long patientId) {
        List<Appointment> appointments = appointmentService.getByPatientId(patientId);
        return Result.success(appointments);
    }

    /**
     * 根据预约号查询
     */
    @GetMapping("/detail/{appointmentNo}")
    public Result<Appointment> getByAppointmentNo(@PathVariable String appointmentNo) {
        Appointment appointment = appointmentService.getByAppointmentNo(appointmentNo);
        return Result.success(appointment);
    }
}
