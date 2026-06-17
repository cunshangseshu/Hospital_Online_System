package com.hospital.dto;

import lombok.Data;

/**
 * 今日接诊统计视图对象
 */
@Data
public class TodayStatsVO {
    private Integer pendingConsults; // 待接诊
    private Integer completedConsults; // 已接诊
}
