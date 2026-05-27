package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 病历Mapper接口
 */
@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {
}
