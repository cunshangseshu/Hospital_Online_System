package com.hospital.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约列表项VO - 用于"我的预约"列表展示和详情
 */
@Data
public class AppointmentListItemVO {

    private Long id;

    private String appointmentNo; // 预约号

    private Long doctorId;

    private String doctorName; // 医生姓名

    private String doctorTitle; // 医生职称

    private Long departmentId;

    private String departmentName; // 科室名称

    private String patientName; // 患者姓名（详情展示用）

    private String appointmentDate; // 预约日期 (yyyy-MM-dd)

    private String timeSlot; // MORNING/AFTERNOON/EVENING

    private String timeSlotText; // 上午/下午/晚上

    private Integer status; // 数据库状态码: 0-待确认,1-已确认,2-已完成,3-已取消,4-已过期

    private String statusText; // 状态英文: PENDING/CONFIRMED/COMPLETED/CANCELLED/EXPIRED（用于CSS类名）

    private String statusLabel; // 状态中文: 待确认/已确认/已完成/已取消/已过期（用于页面显示）

    private String symptom; // 症状描述

    private String cancelReason; // 取消原因

    private Integer rescheduleCount; // 改签次数

    private LocalDateTime createTime; // 创建时间
}
