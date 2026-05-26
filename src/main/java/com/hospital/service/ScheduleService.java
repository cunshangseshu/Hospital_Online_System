package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.entity.Schedule;
import java.time.LocalDate;
import java.util.List;

/**
 * 排班服务接口
 */
public interface ScheduleService extends IService<Schedule> {

    /**
     * 根据医生ID和日期查询排班
     */
    List<Schedule> getByDoctorAndDate(Long doctorId, LocalDate date);

    /**
     * 查询可用号源
     */
    List<Schedule> getAvailableSchedules(Long doctorId, LocalDate startDate, LocalDate endDate);

    /**
     * 减少可用号源
     */
    boolean decreaseAvailableSlots(Long scheduleId);

    /**
     * 增加可用号源（取消预约时）
     */
    boolean increaseAvailableSlots(Long scheduleId);

    /**
     * 为指定日期自动生成排班
     */
    void autoGenerateForDate(LocalDate targetDate);

    /**
     * 批量生成排班
     */
    void generateBatchSchedules(Long doctorId, LocalDate startDate, LocalDate endDate, Integer dailySlots);
}
