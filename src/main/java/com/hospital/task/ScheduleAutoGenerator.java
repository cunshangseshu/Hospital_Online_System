package com.hospital.task;

import com.hospital.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ScheduleAutoGenerator {
    
    @Autowired
    private ScheduleService scheduleService;

    // 每天凌晨 1:00 自动触发，为往后延伸的第 7 天自动铺排无号源医生的基础排班
    @Scheduled(cron = "0 0 1 * * ?")
    public void autoGenerateNextWeekSchedules() {
        LocalDate targetDate = LocalDate.now().plusDays(7);
        scheduleService.autoGenerateForDate(targetDate);
    }
}
