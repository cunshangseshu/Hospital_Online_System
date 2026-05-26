package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生Mapper接口
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
}
