package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.GenerateScheduleRequest;
import com.hospital.entity.Schedule;
import com.hospital.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/doctor/{doctorId}")
    public Result<List<Schedule>> getDoctorSchedules(
            @PathVariable Long doctorId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Schedule> schedules = scheduleService.getAvailableSchedules(doctorId, startDate, endDate);
        return Result.success(schedules);
    }

    @GetMapping("/by-doctor-and-date")
    public Result<List<Schedule>> getDoctorSchedulesByDate(
            @RequestParam Long doctorId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<Schedule> schedules = scheduleService.getAvailableSchedules(doctorId, date, date);
        return Result.success(schedules);
    }

    /**
     * 供Admin使用的：一键批量生成排班
     */
    @PostMapping("/generate")
    public Result<?> generateSchedules(@RequestBody GenerateScheduleRequest req) {
        scheduleService.generateBatchSchedules(
            req.getDoctorId(), 
            req.getStartDate(), 
            req.getEndDate(), 
            req.getDailySlots()
        );
        return Result.success("批量排班生成成功");
    }
}
