package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.HealthProfileVO;
import com.hospital.entity.*;
import com.hospital.security.SecurityUtils;
import com.hospital.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * 导出个人健康档案 - 逆向校验模式
     */
    @GetMapping("/export")
    public void exportProfile(HttpServletResponse response) throws IOException {
        Long userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();
        
        // 【逆向异常抓取 1】: 如果压根没拿不到用户，扔出 401
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthenticated");
            return;
        }
        // 【逆向异常抓取 2】: 绝不允许非患者角色导出
        if (!"PATIENT".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Only patients can export profiles");
            return;
        }

        User user = userService.getById(userId);
        // 【逆向异常抓取 3】: 数据不一致或被锁禁
        if (user == null || user.getStatus() != 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or disabled user account");
            return;
        }

        // --- 核心开始编排 Markdown 文件流结构 ---
        StringBuilder md = new StringBuilder();
        md.append("# 🏥 芯芯数字化医疗 - 个人专属健康档案\n\n");
        md.append("> 导出时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        md.append("## 1. 个人基本信息\n");
        md.append("- **姓名**：").append(user.getRealName() != null ? user.getRealName() : "未填写").append("\n");
        // 脱敏处理手机号
        String safePhone = (user.getPhone() != null && user.getPhone().length() == 11) ? 
                           user.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") : "未填写";
        md.append("- **联系电话**：").append(safePhone).append("\n\n");
        
        md.append("## 2. 线下就诊历史档案\n");
        var appointments = appointmentService.getByPatientId(userId);
        if (appointments.isEmpty()) {
            md.append("*(暂无线下就诊/挂号记录)*\n\n");
        } else {
            for (Appointment appt : appointments) {
                md.append("### 🗓️ ").append(appt.getAppointmentDate()).append("\n");
                md.append("- **状态**：").append(decodeStatus(appt.getStatus())).append("\n");
                md.append("- **时间段**：").append("MORNING".equals(appt.getTimeSlot()) ? "上午" : 
                                               "AFTERNOON".equals(appt.getTimeSlot()) ? "下午" : "晚上").append("\n");
                md.append("- **症状主诉**：").append(appt.getSymptom() != null ? appt.getSymptom() : "无").append("\n\n");
            }
        }
        
        md.append("---\n*本报告由芯芯医疗系统自动生成，妥善保管隐私。*\n");

        // 写入 HttpServletResponse 强制触发下载
        String fileName = "HealthProfile_" + user.getUsername() + ".md";
        response.setContentType("text/markdown");
        response.setCharacterEncoding("UTF-8");
        // 允许跨域时下载的文件名暴露给前端
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        response.getOutputStream().write(md.toString().getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }
    
    // 小辅助函数，转换预约状态码
    private String decodeStatus(Integer status) {
        if(status == null) return "未知";
        switch(status) {
            case 0: return "待确认";
            case 1: return "已确认";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "已过期";
            default: return "未知";
        }
    }
}
