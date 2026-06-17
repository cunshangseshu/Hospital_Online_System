package com.hospital.dto;

import lombok.Data;
import java.util.List;

/**
 * 医生工作台聚合视图对象 (BFF)
 */
@Data
public class DoctorWorkspaceVO {
    private DoctorBaseInfoVO baseInfo;
    private TodayStatsVO todayStats;
    private List<ScheduleItemVO> schedules;
}
