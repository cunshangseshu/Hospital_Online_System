package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.entity.Doctor;
import java.util.List;

/**
 * 医生服务接口
 */
public interface DoctorService extends IService<Doctor> {

    /**
     * 根据科室ID查询医生列表
     */
    List<Doctor> getByDepartmentId(Long deptId);

    /**
     * 搜索医生（按姓名或擅长）
     */
    List<Doctor> searchDoctors(String keyword);

    /**
     * 获取医生详情（包含用户信息）
     */
    Doctor getDoctorDetail(Long id);
}
