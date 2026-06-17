package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.entity.Doctor;
import com.hospital.mapper.DoctorMapper;
import com.hospital.service.DoctorService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医生服务实现类
 */
@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Override
    public List<Doctor> getByDepartmentId(Long deptId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getDeptId, deptId)
               .eq(Doctor::getStatus, 1);
        return list(wrapper);
    }

    @Override
    public List<Doctor> searchDoctors(String keyword) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(Doctor::getTitle, keyword)
                         .or()
                         .like(Doctor::getSpecialty, keyword))
               .eq(Doctor::getStatus, 1);
        return list(wrapper);
    }

    @Override
    public Doctor getDoctorDetail(Long id) {
        return getById(id);
    }
}
