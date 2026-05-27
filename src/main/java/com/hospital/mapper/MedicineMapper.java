package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Medicine;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品Mapper接口
 */
@Mapper
public interface MedicineMapper extends BaseMapper<Medicine> {
}
