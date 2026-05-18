package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.PrescriptionRequest;
import com.hospital.dto.PrescriptionVO;
import com.hospital.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 处方控制器
 */
@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    /**
     * 医生开具处方（AJAX异步提交）
     */
    @PostMapping("/create")
    public Result<PrescriptionVO> create(@RequestBody PrescriptionRequest request) {
        try {
            PrescriptionVO vo = prescriptionService.createPrescription(request);
            return Result.success("处方开具成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取处方详情
     */
    @GetMapping("/{id}")
    public Result<PrescriptionVO> getDetail(@PathVariable Long id) {
        try {
            PrescriptionVO vo = prescriptionService.getPrescriptionDetail(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
