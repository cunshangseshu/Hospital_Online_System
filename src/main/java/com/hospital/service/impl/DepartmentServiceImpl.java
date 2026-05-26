package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.entity.Department;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 科室服务实现类
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public List<Department> getActiveDepartments() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getStatus, 1)
               .orderByAsc(Department::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<Department> searchByName(String name) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Department::getDeptName, name)
               .eq(Department::getStatus, 1);
        return list(wrapper);
    }
}
