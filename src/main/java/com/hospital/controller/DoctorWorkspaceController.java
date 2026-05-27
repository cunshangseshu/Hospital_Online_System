package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.DoctorWorkspaceVO;
import com.hospital.security.SecurityUtils;
import com.hospital.service.DoctorWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 医生工作台聚合控制器 (BFF 架构)
 */
@RestController
@RequestMapping("/api/doctor-workspace")
@CrossOrigin
public class DoctorWorkspaceController {

    @Autowired
    private DoctorWorkspaceService workspaceService;

    /**
     * 获取医生工作台全量聚合数据
     * 安全要求：强制读取上下文 Token 中的用户 ID，不接受参数传入 doctorId 杜绝水平越权
     *
     * @param date 如果为空，默认取当前系统日期
     */
    @GetMapping("/data")
    public Result<DoctorWorkspaceVO> getWorkspaceData(
            @RequestParam(required = false, value = "date") 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        // 参数校验防护：防止恶意传入极其遥远的历史或未来年份，冲击数据库缓存或索引
        if (date != null) {
            int year = date.getYear();
            // 合理的年份跨度：仅允许查询近几年的排班数据，防止跨度过大导致慢查询或缓存击穿
            if (year < 2020 || year > 2030) {
                return Result.error("非法参数：查询年份越界 (合法区间 2020~2030)");
            }
        }

        // 1. 安全校验与强制身份获取
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();

        if (currentUserId == null || !"DOCTOR".equals(role)) {
            return Result.error("越权访问：仅医生角色可访问工作台数据");
        }

        // 2. 调用业务侧组装并查询数据
        try {
            DoctorWorkspaceVO data = workspaceService.getWorkspaceData(currentUserId, date);
            return Result.success("工作台数据加载成功", data);
        } catch (Exception e) {
            return Result.error("工作台数据加载失败: " + e.getMessage());
        }
    }
}
