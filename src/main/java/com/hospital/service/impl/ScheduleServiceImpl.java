package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.entity.Schedule;
import com.hospital.mapper.ScheduleMapper;
import com.hospital.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班服务实现类
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

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
}
