package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.ComplaintRequest;
import com.hospital.entity.Complaint;
import com.hospital.entity.Faq;
import com.hospital.service.ComplaintService;
import com.hospital.service.FaqService;
import com.hospital.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/service")
@CrossOrigin
public class ServiceController {
    @Autowired private FaqService faqService;
    @Autowired private ComplaintService complaintService;

    @GetMapping("/faqs")
    public Result<List<Faq>> getFaqs(@RequestParam(required = false) String category) {
        return Result.success(faqService.getByCategory(category));
    }

    @GetMapping("/faqs/search")
    public Result<List<Faq>> searchFaqs(@RequestParam String keyword) {
        return Result.success(faqService.searchFaqs(keyword));
    }

    @PostMapping("/complaints")
    public Result<Complaint> submitComplaint(@RequestBody ComplaintRequest request) {
        try { return Result.success("提交成功", complaintService.submit(request)); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @GetMapping("/complaints/my-list")
    public Result<List<Complaint>> getMyComplaints() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return Result.error(401, "请先登录");
        return Result.success(complaintService.getByUserId(userId));
    }
}
