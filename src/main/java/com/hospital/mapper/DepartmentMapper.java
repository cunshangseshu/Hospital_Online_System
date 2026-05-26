package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科室Mapper接口
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
