package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Doctor;
import com.hospital.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生控制器 - 处理医生相关AJAX请求
 */
@RestController
@RequestMapping("/api/doctors")
@CrossOrigin
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    /**
     * 根据科室ID获取医生列表（AJAX级联查询）
     */
    @GetMapping("/by-department/{deptId}")
    public Result<List<Doctor>> getByDepartment(@PathVariable Long deptId) {
        List<Doctor> doctors = doctorService.getByDepartmentId(deptId);
        return Result.success(doctors);
    }

    /**
     * 搜索医生（AJAX实时搜索）
     */
    @GetMapping("/search")
    public Result<List<Doctor>> search(@RequestParam String keyword) {
        List<Doctor> doctors = doctorService.searchDoctors(keyword);
        return Result.success(doctors);
    }

    /**
     * 获取医生详情
     */
    @GetMapping("/{id}")
    public Result<Doctor> getById(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorDetail(id);
        return Result.success(doctor);
    }

    /**
     * 获取所有医生列表
     */
    @GetMapping("/list")
    public Result<List<Doctor>> list() {
        List<Doctor> doctors = doctorService.list();
        return Result.success(doctors);
    }
}
