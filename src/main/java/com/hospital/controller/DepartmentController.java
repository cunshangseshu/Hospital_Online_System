package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Department;
import com.hospital.entity.User;
import com.hospital.service.DepartmentService;
import com.hospital.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 科室控制器 - 处理科室相关AJAX请求
 */
@RestController
@RequestMapping("/api/departments")
@CrossOrigin
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserService userService;

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
     * 根据ID获取科室详情及所属名医
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Department department = departmentService.getById(id);
        if (department == null) {
            return Result.error("该科室不存在");
        }
        
        QueryWrapper<User> doctorQa = new QueryWrapper<>();
        doctorQa.eq("department_id", id)
                .eq("role", "DOCTOR")
                .eq("status", 1);
                
        List<User> doctors = userService.list(doctorQa);
        
        Map<String, Object> data = new HashMap<>();
        data.put("department", department);
        data.put("doctors", doctors);
        
        return Result.success(data);
    }
}
