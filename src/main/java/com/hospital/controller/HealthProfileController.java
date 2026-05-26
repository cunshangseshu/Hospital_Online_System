package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.HealthProfileVO;
import com.hospital.entity.*;
import com.hospital.security.SecurityUtils;
import com.hospital.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin
public class HealthProfileController {
    @Autowired private UserService userService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private ConsultationService consultationService;
    @Autowired private PrescriptionService prescriptionService;

    @GetMapping("/my")
    public Result<HealthProfileVO> getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return Result.error(401, "请先登录");
        User user = userService.getById(userId);
        if (user == null) return Result.error("用户不存在");

        HealthProfileVO vo = new HealthProfileVO();
        vo.setPatientName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        vo.setGenderText(user.getGender() == null ? "未知" : user.getGender() == 1 ? "男" : user.getGender() == 2 ? "女" : "未知");

        // 统计数量
        var appointments = appointmentService.getByPatientId(userId);
        vo.setAppointmentCount(appointments.size());
        var consultations = consultationService.getMyConsultations(userId, "PATIENT", null, 1, 5);
        vo.setConsultationCount(consultations.getList().size());

        // 最近就诊
        vo.setRecentAppointments(appointments.stream().limit(3).map(a -> {
            var v = new com.hospital.dto.AppointmentListItemVO();
            v.setId(a.getId()); v.setAppointmentNo(a.getAppointmentNo());
            v.setAppointmentDate(a.getAppointmentDate().toString());
            v.setTimeSlot(a.getTimeSlot()); v.setStatus(a.getStatus());
            return v;
        }).collect(Collectors.toList()));

        return Result.success(vo);
    }
}
