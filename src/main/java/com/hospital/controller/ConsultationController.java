package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.*;
import com.hospital.security.SecurityUtils;
import com.hospital.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线问诊控制器
 */
@RestController
@RequestMapping("/api/consultations")
@CrossOrigin
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    /**
     * 发起问诊（AJAX异步提交）
     */
    @PostMapping("/create")
    public Result<ConsultationVO> create(@RequestBody ConsultationRequest request) {
        try {
            ConsultationVO vo = consultationService.createConsultation(request);
            return Result.success("问诊发起成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送消息（AJAX异步提交）
     */
    @PostMapping("/{id}/message")
    public Result<ConsultationMessageVO> sendMessage(@PathVariable Long id, @RequestBody SendMessageRequest request) {
        try {
            ConsultationMessageVO vo = consultationService.sendMessage(id, request);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取问诊详情（含消息列表、处方）
     */
    @GetMapping("/{id}")
    public Result<ConsultationVO> getDetail(@PathVariable Long id) {
        try {
            ConsultationVO vo = consultationService.getConsultationDetail(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的问诊列表（分页）
     */
    @GetMapping("/my-list")
    public Result<PageResult<ConsultationVO>> getMyList(
            @RequestParam String userType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            PageResult<ConsultationVO> result = consultationService.getMyConsultations(userId, userType, status, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 医生接诊
     */
    @PostMapping("/{id}/accept")
    public Result<String> accept(@PathVariable Long id) {
        try {
            consultationService.acceptConsultation(id);
            return Result.success("已接诊", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 结束问诊
     */
    @PostMapping("/{id}/finish")
    public Result<String> finish(@PathVariable Long id) {
        try {
            consultationService.finishConsultation(id);
            return Result.success("问诊已结束", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * AJAX轮询获取新消息
     */
    @GetMapping("/{id}/messages")
    public Result<List<ConsultationMessageVO>> getMessages(
            @PathVariable Long id,
            @RequestParam(required = false) Long afterMessageId) {
        try {
            List<ConsultationMessageVO> messages = consultationService.getNewMessages(id, afterMessageId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
