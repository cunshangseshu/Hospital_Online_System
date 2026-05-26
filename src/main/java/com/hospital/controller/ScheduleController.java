package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Schedule;
import com.hospital.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班控制器 - 处理号源查询等AJAX请求
 */
@RestController
@RequestMapping("/api/schedules")
@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 查询医生的可用排班（AJAX动态加载号源）
     */
    @GetMapping("/available")
    public Result<List<Schedule>> getAvailableSchedules(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Schedule> schedules = scheduleService.getAvailableSchedules(doctorId, startDate, endDate);
        return Result.success(schedules);
    }

    /**
     * 根据医生ID和日期查询排班
     */
    @GetMapping("/by-doctor-and-date")
    public Result<List<Schedule>> getByDoctorAndDate(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Schedule> schedules = scheduleService.getByDoctorAndDate(doctorId, date);
        return Result.success(schedules);
    }
}
