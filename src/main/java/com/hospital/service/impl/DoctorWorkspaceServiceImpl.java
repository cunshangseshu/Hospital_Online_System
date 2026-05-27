package com.hospital.service.impl;

import com.hospital.dto.DoctorBaseInfoVO;
import com.hospital.dto.DoctorWorkspaceVO;
import com.hospital.dto.ScheduleItemVO;
import com.hospital.dto.TodayStatsVO;
import com.hospital.mapper.DoctorWorkspaceMapper;
import com.hospital.service.DoctorWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生工作台业务实现类
 */
@Service
public class DoctorWorkspaceServiceImpl implements DoctorWorkspaceService {

    @Autowired
    private DoctorWorkspaceMapper workspaceMapper;

    @Override
    public DoctorWorkspaceVO getWorkspaceData(Long doctorUserId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        // 1. 获取基本信息 (包含科室名)
        DoctorBaseInfoVO baseInfo = workspaceMapper.getDoctorBaseInfo(doctorUserId);
        if (baseInfo == null) {
            throw new RuntimeException("未能查询到医生信息，请确认身份");
        }

        // 2. 获取统计数据
        Integer pendingCount = workspaceMapper.getPendingCount(doctorUserId, date);
        Integer completedCount = workspaceMapper.getCompletedCount(doctorUserId, date);

        TodayStatsVO todayStats = new TodayStatsVO();
        todayStats.setPendingConsults(pendingCount != null ? pendingCount : 0);
        todayStats.setCompletedConsults(completedCount != null ? completedCount : 0);

        // 3. 获取排班情况列表
        List<ScheduleItemVO> schedules = workspaceMapper.getSchedules(doctorUserId, date);

        // 4. 组装 BFF 数据结构返回
        DoctorWorkspaceVO workspaceVO = new DoctorWorkspaceVO();
        workspaceVO.setBaseInfo(baseInfo);
        workspaceVO.setTodayStats(todayStats);
        workspaceVO.setSchedules(schedules);

        return workspaceVO;
    }
}
