package com.hospital.dto;

import lombok.Data;

/**
 * 医生基本信息视图对象
 */
@Data
public class DoctorBaseInfoVO {
    private Long id;
    private String realName;
    private String title;
    private String departmentName;
    private String phone;
    private String status;
}
