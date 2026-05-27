package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.entity.Schedule;
import com.hospital.entity.User;
import com.hospital.mapper.ScheduleMapper;
import com.hospital.service.ScheduleService;
import com.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 排班服务实现类
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Autowired
    private UserService userService;

    @Override
    public List<Schedule> getByDoctorAndDate(Long doctorId, LocalDate date) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
               .eq(Schedule::getWorkDate, date)
               .eq(Schedule::getStatus, 1);
        return list(wrapper);
    }

    @Override
    public List<Schedule> getAvailableSchedules(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
               .ge(Schedule::getWorkDate, startDate)
               .le(Schedule::getWorkDate, endDate)
               .gt(Schedule::getAvailableSlots, 0)
               .eq(Schedule::getStatus, 1)
               .orderByAsc(Schedule::getWorkDate);
        return list(wrapper);
    }

    @Override
    @Transactional
    public boolean decreaseAvailableSlots(Long scheduleId) {
        Schedule schedule = getById(scheduleId);
        if (schedule == null || schedule.getAvailableSlots() <= 0) {
            return false;
        }
        schedule.setAvailableSlots(schedule.getAvailableSlots() - 1);
        return updateById(schedule);
    }

    @Override
    @Transactional
    public boolean increaseAvailableSlots(Long scheduleId) {
        Schedule schedule = getById(scheduleId);
        if (schedule == null) {
            return false;
        }
        schedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
        return updateById(schedule);
    }

    @Override
    @Transactional
    public void autoGenerateForDate(LocalDate targetDate) {
        QueryWrapper<User> doctorQa = new QueryWrapper<>();
        doctorQa.eq("role", "DOCTOR").eq("status", 1);
        List<User> doctors = userService.list(doctorQa);
        
        for (User doctor : doctors) {
            if (getByDoctorAndDate(doctor.getId(), targetDate).isEmpty()) {
                generateSingleDaySchedule(doctor.getId(), targetDate, 20);
            }
        }
    }

    @Override
    @Transactional
    public void generateBatchSchedules(Long doctorId, LocalDate startDate, LocalDate endDate, Integer dailySlots) {
        if (dailySlots == null || dailySlots <= 0) {
            dailySlots = 20; // 默认20个号源
        }
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (getByDoctorAndDate(doctorId, current).isEmpty()) {
                generateSingleDaySchedule(doctorId, current, dailySlots);
            }
            current = current.plusDays(1);
        }
    }

    private void generateSingleDaySchedule(Long doctorId, LocalDate date, int slots) {
        List<Schedule> schedules = new ArrayList<>();
        
        // 上午排班
        Schedule amSchedule = new Schedule();
        amSchedule.setDoctorId(doctorId);
        amSchedule.setWorkDate(date);
        amSchedule.setTimeSlot("MORNING");
        amSchedule.setTotalSlots(slots);
        amSchedule.setAvailableSlots(slots);
        amSchedule.setStatus(1);
        schedules.add(amSchedule);

        // 下午排班
        Schedule pmSchedule = new Schedule();
        pmSchedule.setDoctorId(doctorId);
        pmSchedule.setWorkDate(date);
        pmSchedule.setTimeSlot("AFTERNOON");
        pmSchedule.setTotalSlots(slots);
        pmSchedule.setAvailableSlots(slots);
        pmSchedule.setStatus(1);
        schedules.add(pmSchedule);

        saveBatch(schedules);
    }
}
