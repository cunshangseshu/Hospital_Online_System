package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.AppointmentListItemVO;
import com.hospital.dto.AppointmentRequest;
import com.hospital.dto.CancelRequest;
import com.hospital.dto.PageResult;
import com.hospital.dto.RescheduleRequest;
import com.hospital.entity.Appointment;
import com.hospital.entity.Schedule;
import com.hospital.security.SecurityUtils;
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
     * 取消预约（AJAX异步提交）- 对齐前端 POST /{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable Long id, @RequestBody CancelRequest request) {
        try {
            appointmentService.cancelAppointment(id, request.getCancelReason());
            return Result.success("取消成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的预约列表（分页+筛选+搜索）
     */
    @GetMapping("/my-list")
    public Result<PageResult<AppointmentListItemVO>> getMyAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Long patientId = getCurrentUserId();
            if (patientId == null) {
                return Result.error(401, "请先登录");
            }

            PageResult<AppointmentListItemVO> result = appointmentService.getMyAppointments(patientId, status, keyword, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    public Result<AppointmentListItemVO> getAppointmentDetail(@PathVariable Long id) {
        try {
            AppointmentListItemVO detail = appointmentService.getAppointmentDetail(id);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 改签预约 - 对齐前端 POST /{id}/reschedule
     */
    @PostMapping("/{id}/reschedule")
    public Result<String> reschedule(
            @PathVariable Long id,
            @RequestBody RescheduleRequest request) {
        try {
            appointmentService.rescheduleAppointment(id, request.getNewScheduleId(),
                    request.getNewAppointmentDate(), request.getNewTimeSlot());
            return Result.success("改签成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取可改签的号源列表
     */
    @GetMapping("/reschedule-schedules")
    public Result<List<Schedule>> getRescheduleSchedules(
            @RequestParam Long doctorId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<Schedule> schedules = appointmentService.getRescheduleSchedules(doctorId, startDate, endDate);
            return Result.success(schedules);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询患者的预约列表（旧接口，保留兼容）
     */
    @GetMapping("/my-appointments")
    public Result<List<Appointment>> getMyAppointmentsOld(@RequestParam Long patientId) {
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

    /**
     * 从JWT Token中获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }
}
