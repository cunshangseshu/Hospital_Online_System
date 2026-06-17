package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.LabReportVO;
import com.hospital.service.LabReportService;
import com.hospital.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports/lab")
@CrossOrigin
public class LabReportController {
    @Autowired private LabReportService labReportService;

    @GetMapping("/my-list")
    public Result<List<LabReportVO>> getMyList() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return Result.error(401, "请先登录");
        return Result.success(labReportService.getByPatientId(userId));
    }

    @GetMapping("/{id}")
    public Result<LabReportVO> getDetail(@PathVariable Long id) {
        try { return Result.success(labReportService.getReportDetail(id)); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }
}
