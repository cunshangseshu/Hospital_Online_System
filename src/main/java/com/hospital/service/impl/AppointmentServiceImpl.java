package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.dto.AppointmentRequest;
import com.hospital.entity.Appointment;
import com.hospital.entity.Schedule;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.service.AppointmentService;
import com.hospital.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 预约服务实现类
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private ScheduleService scheduleService;

    @Override
    @Transactional
    public Appointment createAppointment(AppointmentRequest request) {
        // 检查号源
        Schedule schedule = scheduleService.getById(request.getScheduleId());
        if (schedule == null || schedule.getAvailableSlots() <= 0) {
            throw new RuntimeException("号源不足，请选择其他时间");
        }

        // 减少可用号源
        if (!scheduleService.decreaseAvailableSlots(request.getScheduleId())) {
            throw new RuntimeException("预约失败，请稍后重试");
        }

        // 创建预约记录
        Appointment appointment = new Appointment();
        appointment.setPatientId(getCurrentUserId());
        appointment.setScheduleId(request.getScheduleId());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setTimeSlot(request.getTimeSlot());
        appointment.setSymptom(request.getSymptom());
        appointment.setStatus(0); // 待确认
        appointment.setAppointmentNo(generateAppointmentNo());

        save(appointment);
        return appointment;
    }

    @Override
    @Transactional
    public boolean cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }

        if (appointment.getStatus() >= 2) {
            throw new RuntimeException("该预约无法取消");
        }

        // 更新预约状态
        appointment.setStatus(3); // 已取消
        appointment.setCancelReason(reason);
        updateById(appointment);

        // 恢复号源
        scheduleService.increaseAvailableSlots(appointment.getScheduleId());

        return true;
    }

    @Override
    public List<Appointment> getByPatientId(Long patientId) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId, patientId)
               .orderByDesc(Appointment::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public List<Appointment> getByDoctorId(Long doctorId, LocalDate date) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getDoctorId, doctorId);
        if (date != null) {
            wrapper.eq(Appointment::getAppointmentDate, date);
        }
        wrapper.orderByAsc(Appointment::getAppointmentDate);
        return list(wrapper);
    }

    @Override
    public Appointment getByAppointmentNo(String appointmentNo) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getAppointmentNo, appointmentNo);
        return getOne(wrapper);
    }

    /**
     * 生成预约号
     */
    private String generateAppointmentNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "APT" + dateStr + (int)(Math.random() * 1000);
    }

    /**
     * 获取当前用户ID（简化版，实际应从SecurityContext获取）
     */
    private Long getCurrentUserId() {
        // TODO: 从JWT Token中获取
        return 1L;
    }
}
