package com.hospital.service;

import com.hospital.dto.DoctorWorkspaceVO;
import java.time.LocalDate;

/**
 * 医生工作台业务接口
 */
public interface DoctorWorkspaceService {
    
    /**
     * 获取医生工作台聚合数据
     */
    DoctorWorkspaceVO getWorkspaceData(Long doctorUserId, LocalDate date);
}
