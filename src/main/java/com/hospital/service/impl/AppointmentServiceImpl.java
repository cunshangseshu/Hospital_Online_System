package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.dto.AppointmentListItemVO;
import com.hospital.dto.AppointmentRequest;
import com.hospital.dto.PageResult;
import com.hospital.entity.Appointment;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.security.SecurityUtils;
import com.hospital.service.AppointmentService;
import com.hospital.service.DepartmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.ScheduleService;
import com.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserService userService;

    // 状态映射: 数据库数字 -> API字符串枚举
    private static final String[] STATUS_MAP = {"PENDING", "CONFIRMED", "COMPLETED", "CANCELLED", "EXPIRED"};
    private static final String[] STATUS_TEXT_MAP = {"待确认", "已确认", "已完成", "已取消", "已过期"};

    // 时间段映射
    private static final java.util.Map<String, String> TIME_SLOT_MAP = new java.util.HashMap<>();
    static {
        TIME_SLOT_MAP.put("MORNING", "上午");
        TIME_SLOT_MAP.put("AFTERNOON", "下午");
        TIME_SLOT_MAP.put("EVENING", "晚上");
    }

    @Override
    @Transactional
    public Appointment createAppointment(AppointmentRequest request) {
        // 检查号源
        Schedule schedule = scheduleService.getById(request.getScheduleId());
        if (schedule == null || schedule.getAvailableSlots() <= 0) {
            throw new RuntimeException("号源不足，请选择其他时间");
        }

        // 减少可用号源
        if (!scheduleService.decreaseAvailableSlots(request.getScheduleId())) {
            throw new RuntimeException("预约失败，请稍后重试");
        }

        // 通过schedule获取doctor,再通过doctor获取deptId
        Doctor doctor = doctorService.getById(schedule.getDoctorId());
        if (doctor == null) {
            throw new RuntimeException("医生信息不存在");
        }
        if (request.getDoctorId() != null && !request.getDoctorId().equals(schedule.getDoctorId())) {
            throw new RuntimeException("预约医生与号源不匹配");
        }

        // 创建预约记录
        Appointment appointment = new Appointment();
        appointment.setPatientId(getCurrentUserId());
        appointment.setScheduleId(request.getScheduleId());
        appointment.setDoctorId(schedule.getDoctorId());
        appointment.setDeptId(doctor.getDeptId()); // 自动设置科室ID
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setTimeSlot(request.getTimeSlot());
        appointment.setSymptom(request.getSymptom());
        appointment.setStatus(0); // 待确认
        appointment.setAppointmentNo(generateAppointmentNo());

        save(appointment);
        return appointment;
    }

    @Override
    @Transactional
    public boolean cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }
        ensureAppointmentOwner(appointment);

        if (appointment.getStatus() >= 2) {
            throw new RuntimeException("该预约无法取消");
        }

        // 更新预约状态
        appointment.setStatus(3); // 已取消
        appointment.setCancelReason(reason);
        updateById(appointment);

        // 恢复号源
        scheduleService.increaseAvailableSlots(appointment.getScheduleId());

        return true;
    }

    @Override
    public List<Appointment> getByPatientId(Long patientId) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId, patientId)
               .orderByDesc(Appointment::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public PageResult<AppointmentListItemVO> getMyAppointments(Long patientId, String status, String keyword, Integer pageNum, Integer pageSize) {
        // 默认分页参数
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        // 构建查询条件
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId, patientId);

        // 状态筛选
        if (StringUtils.hasText(status)) {
            Integer statusCode = convertStatusToCode(status);
            if (statusCode != null) {
                wrapper.eq(Appointment::getStatus, statusCode);
            }
        }

        // 关键字搜索(预约号或医生姓名)
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Appointment::getAppointmentNo, keyword)
                    .or()
                    .apply("doctor_id IN (SELECT id FROM doctor WHERE user_id IN (SELECT id FROM sys_user WHERE real_name LIKE {0}))", "%" + keyword + "%"));
        }

        wrapper.orderByDesc(Appointment::getCreatedAt);

        // 分页查询
        Page<Appointment> page = new Page<>(pageNum, pageSize);
        Page<Appointment> resultPage = page(page, wrapper);

        // 转换为VO
        List<AppointmentListItemVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), pageNum, pageSize, voList);
    }

    @Override
    public List<Appointment> getByDoctorId(Long doctorId, LocalDate date) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getDoctorId, doctorId);
        if (date != null) {
            wrapper.eq(Appointment::getAppointmentDate, date);
        }
        wrapper.orderByAsc(Appointment::getAppointmentDate);
        return list(wrapper);
    }

    @Override
    public Appointment getByAppointmentNo(String appointmentNo) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getAppointmentNo, appointmentNo);
        return getOne(wrapper);
    }

    @Override
    public AppointmentListItemVO getAppointmentDetail(Long id) {
        Appointment appointment = getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }
        ensureAppointmentOwner(appointment);
        return convertToVO(appointment);
    }

    @Override
    @Transactional
    public boolean rescheduleAppointment(Long oldAppointmentId, Long newScheduleId, String newAppointmentDate, String newTimeSlot) {
        // 获取原预约
        Appointment oldAppointment = getById(oldAppointmentId);
        if (oldAppointment == null) {
            throw new RuntimeException("原预约记录不存在");
        }
        ensureAppointmentOwner(oldAppointment);

        // 检查原预约状态
        if (oldAppointment.getStatus() >= 2) {
            throw new RuntimeException("该预约无法改签");
        }

        // 检查新号源
        Schedule newSchedule = scheduleService.getById(newScheduleId);
        if (newSchedule == null || newSchedule.getAvailableSlots() <= 0) {
            throw new RuntimeException("新号源不足");
        }
        if (!newSchedule.getDoctorId().equals(oldAppointment.getDoctorId())) {
            throw new RuntimeException("只能改签到同一医生的号源");
        }

        // 减少新号源
        if (!scheduleService.decreaseAvailableSlots(newScheduleId)) {
            throw new RuntimeException("改签失败，请稍后重试");
        }

        // 恢复原号源
        scheduleService.increaseAvailableSlots(oldAppointment.getScheduleId());

        // 创建新的预约记录
        Appointment newAppointment = new Appointment();
        newAppointment.setPatientId(oldAppointment.getPatientId());
        newAppointment.setScheduleId(newScheduleId);
        newAppointment.setDoctorId(oldAppointment.getDoctorId());
        newAppointment.setDeptId(oldAppointment.getDeptId());
        newAppointment.setAppointmentDate(LocalDate.parse(newAppointmentDate));
        newAppointment.setTimeSlot(newTimeSlot);
        newAppointment.setSymptom(oldAppointment.getSymptom());
        newAppointment.setStatus(0); // 待确认
        newAppointment.setAppointmentNo(generateAppointmentNo());

        save(newAppointment);

        // 更新原预约状态为已取消
        oldAppointment.setStatus(3); // 已取消
        oldAppointment.setCancelReason("改签至新预约: " + newAppointment.getAppointmentNo());
        updateById(oldAppointment);

        return true;
    }

    @Override
    public List<Schedule> getRescheduleSchedules(Long doctorId, String startDate, String endDate) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
               .ge(Schedule::getWorkDate, LocalDate.parse(startDate))
               .le(Schedule::getWorkDate, LocalDate.parse(endDate))
               .gt(Schedule::getAvailableSlots, 0)
               .eq(Schedule::getStatus, 1)
               .orderByAsc(Schedule::getWorkDate)
               .orderByAsc(Schedule::getTimeSlot);

        return scheduleService.list(wrapper);
    }

    /**
     * 生成预约号
     */
    private String generateAppointmentNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "APT" + dateStr + (int)(Math.random() * 1000);
    }

    /**
     * 获取当前用户ID（从SecurityContext中获取）
     */
    private Long getCurrentUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    /**
     * 患者端预约操作只能访问自己的预约，避免通过ID越权查看、取消或改签他人预约。
     */
    private void ensureAppointmentOwner(Appointment appointment) {
        Long userId = getCurrentUserId();
        if (!userId.equals(appointment.getPatientId())) {
            throw new RuntimeException("无权操作该预约");
        }
    }

    /**
     * 将Appointment转换为AppointmentListItemVO
     */
    private AppointmentListItemVO convertToVO(Appointment appointment) {
        AppointmentListItemVO vo = new AppointmentListItemVO();
        vo.setId(appointment.getId());
        vo.setAppointmentNo(appointment.getAppointmentNo());
        vo.setDoctorId(appointment.getDoctorId());
        vo.setDepartmentId(appointment.getDeptId());
        vo.setAppointmentDate(appointment.getAppointmentDate().toString());
        vo.setTimeSlot(appointment.getTimeSlot());
        vo.setTimeSlotText(TIME_SLOT_MAP.getOrDefault(appointment.getTimeSlot(), ""));
        vo.setStatus(appointment.getStatus());
        vo.setStatusText(convertStatusToText(appointment.getStatus()));
        vo.setStatusLabel(STATUS_TEXT_MAP[appointment.getStatus() != null && appointment.getStatus() < STATUS_TEXT_MAP.length ? appointment.getStatus() : 0]);
        vo.setSymptom(appointment.getSymptom());
        vo.setCreateTime(appointment.getCreatedAt());
        vo.setCancelReason(appointment.getCancelReason());

        // 查询患者信息
        com.hospital.entity.User patient = userService.getById(appointment.getPatientId());
        if (patient != null) {
            vo.setPatientName(patient.getRealName());
        }

        // 查询医生信息
        Doctor doctor = doctorService.getById(appointment.getDoctorId());
        if (doctor != null) {
            vo.setDoctorTitle(doctor.getTitle());
            
            // 查询医生姓名
            com.hospital.entity.User user = userService.getById(doctor.getUserId());
            if (user != null) {
                vo.setDoctorName(user.getRealName());
            }
        }

        // 查询科室名称
        Department dept = departmentService.getById(appointment.getDeptId());
        if (dept != null) {
            vo.setDepartmentName(dept.getDeptName());
        }

        return vo;
    }

    /**
     * 状态码转文本枚举
     */
    private String convertStatusToText(Integer statusCode) {
        if (statusCode == null || statusCode < 0 || statusCode >= STATUS_MAP.length) {
            return "UNKNOWN";
        }
        return STATUS_MAP[statusCode];
    }

    /**
     * 文本枚举转状态码
     */
    private Integer convertStatusToCode(String statusText) {
        for (int i = 0; i < STATUS_MAP.length; i++) {
            if (STATUS_MAP[i].equals(statusText)) {
                return i;
            }
        }
        return null;
    }
}
