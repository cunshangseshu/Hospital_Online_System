package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.entity.Department;
import java.util.List;

/**
 * 科室服务接口
 */
public interface DepartmentService extends IService<Department> {

    /**
     * 获取所有启用的科室列表
     */
    List<Department> getActiveDepartments();

    /**
     * 根据名称搜索科室
     */
    List<Department> searchByName(String name);
}
