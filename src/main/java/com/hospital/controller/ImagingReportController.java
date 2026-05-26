package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.ImagingReportVO;
import com.hospital.service.ImagingReportService;
import com.hospital.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports/imaging")
@CrossOrigin
public class ImagingReportController {
    @Autowired private ImagingReportService imagingReportService;

    @GetMapping("/my-list")
    public Result<List<ImagingReportVO>> getMyList() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return Result.error(401, "请先登录");
        return Result.success(imagingReportService.getByPatientId(userId));
    }

    @GetMapping("/{id}")
    public Result<ImagingReportVO> getDetail(@PathVariable Long id) {
        try { return Result.success(imagingReportService.getReportDetail(id)); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }
}
