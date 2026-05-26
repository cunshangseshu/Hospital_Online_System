package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Department;
import com.hospital.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科室控制器 - 处理科室相关AJAX请求
 */
@RestController
@RequestMapping("/api/departments")
@CrossOrigin
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 获取所有启用的科室列表（AJAX动态加载）
     */
    @GetMapping("/list")
    public Result<List<Department>> list() {
        List<Department> departments = departmentService.getActiveDepartments();
        return Result.success(departments);
    }

    /**
     * 搜索科室（AJAX实时搜索）
     */
    @GetMapping("/search")
    public Result<List<Department>> search(@RequestParam String keyword) {
        List<Department> departments = departmentService.searchByName(keyword);
        return Result.success(departments);
    }

    /**
     * 根据ID获取科室详情
     */
    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        Department department = departmentService.getById(id);
        return Result.success(department);
    }
}
