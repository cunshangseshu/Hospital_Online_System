package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.dto.*;
import com.hospital.entity.*;
import com.hospital.mapper.ConsultationMapper;
import com.hospital.mapper.ConsultationMessageMapper;
import com.hospital.security.SecurityUtils;
import com.hospital.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在线问诊服务实现类
 */
@Service
public class ConsultationServiceImpl extends ServiceImpl<ConsultationMapper, Consultation> implements ConsultationService {

    @Autowired
    private ConsultationMessageMapper messageMapper;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private PrescriptionService prescriptionService;

    private static final String[] STATUS_MAP = {"待接诊", "问诊中", "已完成", "已关闭"};

    @Override
    @Transactional
    public ConsultationVO createConsultation(ConsultationRequest request) {
        Long patientId = SecurityUtils.getCurrentUserId();
        if (patientId == null) {
            throw new RuntimeException("请先登录");
        }

        Consultation consultation = new Consultation();
        consultation.setConsultationNo(generateNo("CONS"));
        consultation.setPatientId(patientId);
        consultation.setDoctorId(request.getDoctorId());
        consultation.setDeptId(request.getDeptId());
        consultation.setType(request.getType() != null ? request.getType() : "TEXT");
        consultation.setStatus(0); // 待接诊
        consultation.setSymptomDescription(request.getSymptomDescription());

        save(consultation);

        // 插入系统消息
        insertSystemMessage(consultation.getId(), "问诊已发起，请等待医生接诊...");

        return convertToVO(consultation);
    }

    @Override
    @Transactional
    public ConsultationMessageVO sendMessage(Long consultationId, SendMessageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        Consultation consultation = getById(consultationId);
        if (consultation == null) {
            throw new RuntimeException("问诊记录不存在");
        }
        if (consultation.getStatus() >= 2) {
            throw new RuntimeException("问诊已结束");
        }

        // 判断发送者是患者还是医生
        String senderType;
        if (userId.equals(consultation.getPatientId())) {
            senderType = "PATIENT";
        } else if (isAssignedDoctor(userId, consultation.getDoctorId())) {
            senderType = "DOCTOR";
        } else {
            throw new RuntimeException("无权发送消息");
        }

        // 如果是患者发消息且问诊状态为待接诊，保持不变（等待医生接诊后回复）
        if ("DOCTOR".equals(senderType) && consultation.getStatus() == 0) {
            consultation.setStatus(1); // 医生首次回复 → 问诊中
            consultation.setStartedAt(LocalDateTime.now());
            updateById(consultation);
        }

        ConsultationMessage message = new ConsultationMessage();
        message.setConsultationId(consultationId);
        message.setSenderId(userId);
        message.setSenderType(senderType);
        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : "TEXT");
        message.setContent(request.getContent());
        message.setIsRead(0);

        messageMapper.insert(message);

        return convertMessageToVO(message);
    }

    @Override
    public ConsultationVO getConsultationDetail(Long consultationId) {
        Consultation consultation = getById(consultationId);
        if (consultation == null) {
            throw new RuntimeException("问诊记录不存在");
        }
        ensureParticipant(consultation);

        ConsultationVO vo = convertToVO(consultation);

        // 加载消息列表
        List<ConsultationMessageVO> messages = getMessagesByConsultationId(consultationId);
        vo.setMessages(messages);

        // 加载处方列表
        List<PrescriptionVO> prescriptions = prescriptionService.getByConsultationId(consultationId);
        vo.setPrescriptions(prescriptions);

        return vo;
    }

    @Override
    public PageResult<ConsultationVO> getMyConsultations(Long userId, String userType, String status, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();

        if ("PATIENT".equals(userType)) {
            wrapper.eq(Consultation::getPatientId, userId);
        } else if ("DOCTOR".equals(userType)) {
            Doctor doctor = getDoctorByUserId(userId);
            if (doctor == null) {
                throw new RuntimeException("医生信息不存在");
            }
            wrapper.eq(Consultation::getDoctorId, doctor.getId());
        }

        if (StringUtils.hasText(status)) {
            Integer statusCode = parseStatus(status);
            if (statusCode != null) {
                wrapper.eq(Consultation::getStatus, statusCode);
            }
        }

        wrapper.orderByDesc(Consultation::getCreatedAt);

        Page<Consultation> page = new Page<>(pageNum, pageSize);
        Page<Consultation> resultPage = page(page, wrapper);

        List<ConsultationVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), pageNum, pageSize, voList);
    }

    @Override
    @Transactional
    public void acceptConsultation(Long consultationId) {
        Consultation consultation = getById(consultationId);
        if (consultation == null) {
            throw new RuntimeException("问诊记录不存在");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        if (!isAssignedDoctor(userId, consultation.getDoctorId())) {
            throw new RuntimeException("无权接诊该问诊");
        }
        if (consultation.getStatus() != 0) {
            throw new RuntimeException("当前状态无法接诊");
        }

        consultation.setStatus(1); // 问诊中
        consultation.setStartedAt(LocalDateTime.now());
        updateById(consultation);

        insertSystemMessage(consultationId, "医生已接诊，请描述您的症状。");
    }

    @Override
    @Transactional
    public void finishConsultation(Long consultationId) {
        Consultation consultation = getById(consultationId);
        if (consultation == null) {
            throw new RuntimeException("问诊记录不存在");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null || (!userId.equals(consultation.getPatientId()) && !isAssignedDoctor(userId, consultation.getDoctorId()))) {
            throw new RuntimeException("无权结束该问诊");
        }

        consultation.setStatus(2); // 已完成
        consultation.setFinishedAt(LocalDateTime.now());
        updateById(consultation);

        insertSystemMessage(consultationId, "本次问诊已结束，感谢您的咨询！");
    }

    @Override
    public List<ConsultationMessageVO> getNewMessages(Long consultationId, Long afterMessageId) {
        Consultation consultation = getById(consultationId);
        if (consultation == null) {
            throw new RuntimeException("问诊记录不存在");
        }
        ensureParticipant(consultation);

        LambdaQueryWrapper<ConsultationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationMessage::getConsultationId, consultationId);
        if (afterMessageId != null && afterMessageId > 0) {
            wrapper.gt(ConsultationMessage::getId, afterMessageId);
        }
        wrapper.orderByAsc(ConsultationMessage::getCreatedAt);

        List<ConsultationMessage> messages = messageMapper.selectList(wrapper);
        return messages.stream().map(this::convertMessageToVO).collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    private List<ConsultationMessageVO> getMessagesByConsultationId(Long consultationId) {
        LambdaQueryWrapper<ConsultationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationMessage::getConsultationId, consultationId)
               .orderByAsc(ConsultationMessage::getCreatedAt);
        return messageMapper.selectList(wrapper).stream()
                .map(this::convertMessageToVO)
                .collect(Collectors.toList());
    }

    private ConsultationVO convertToVO(Consultation c) {
        ConsultationVO vo = new ConsultationVO();
        vo.setId(c.getId());
        vo.setConsultationNo(c.getConsultationNo());
        vo.setPatientId(c.getPatientId());
        vo.setDoctorId(c.getDoctorId());
        vo.setDeptId(c.getDeptId());
        vo.setType(c.getType());
        vo.setTypeText("VIDEO".equals(c.getType()) ? "视频问诊" : "文字问诊");
        vo.setStatus(c.getStatus());
        vo.setStatusText(c.getStatus() != null && c.getStatus() < STATUS_MAP.length ? STATUS_MAP[c.getStatus()] : "未知");
        vo.setSymptomDescription(c.getSymptomDescription());
        vo.setDoctorNote(c.getDoctorNote());
        vo.setStartedAt(c.getStartedAt());
        vo.setCreatedAt(c.getCreatedAt());

        // 患者姓名
        User patient = userService.getById(c.getPatientId());
        if (patient != null) {
            vo.setPatientName(patient.getRealName());
        }

        // 医生信息
        Doctor doctor = doctorService.getById(c.getDoctorId());
        if (doctor != null) {
            vo.setDoctorTitle(doctor.getTitle());
            User doctorUser = userService.getById(doctor.getUserId());
            if (doctorUser != null) {
                vo.setDoctorName(doctorUser.getRealName());
            }
        }

        // 科室名称
        Department dept = departmentService.getById(c.getDeptId());
        if (dept != null) {
            vo.setDeptName(dept.getDeptName());
        }

        return vo;
    }

    private ConsultationMessageVO convertMessageToVO(ConsultationMessage m) {
        ConsultationMessageVO vo = new ConsultationMessageVO();
        vo.setId(m.getId());
        vo.setSenderId(m.getSenderId());
        vo.setSenderType(m.getSenderType());
        vo.setMessageType(m.getMessageType());
        vo.setContent(m.getContent());
        vo.setRefPrescriptionId(m.getRefPrescriptionId());
        vo.setCreatedAt(m.getCreatedAt());

        // 格式化时间
        if (m.getCreatedAt() != null) {
            vo.setTime(m.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        // 发送者姓名
        if ("SYSTEM".equals(m.getSenderType())) {
            vo.setSenderName("系统消息");
        } else {
            User user = userService.getById(m.getSenderId());
            if (user != null) {
                vo.setSenderName(user.getRealName());
            }
        }

        return vo;
    }

    private void insertSystemMessage(Long consultationId, String content) {
        ConsultationMessage msg = new ConsultationMessage();
        msg.setConsultationId(consultationId);
        msg.setSenderId(0L);
        msg.setSenderType("SYSTEM");
        msg.setMessageType("TEXT");
        msg.setContent(content);
        msg.setIsRead(1);
        messageMapper.insert(msg);
    }

    private Doctor getDoctorByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return doctorService.getOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
    }

    private boolean isAssignedDoctor(Long userId, Long doctorId) {
        Doctor doctor = getDoctorByUserId(userId);
        return doctor != null && doctor.getId().equals(doctorId);
    }

    private void ensureParticipant(Consultation consultation) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null || (!userId.equals(consultation.getPatientId()) && !isAssignedDoctor(userId, consultation.getDoctorId()))) {
            throw new RuntimeException("无权访问该问诊");
        }
    }

    private Integer parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        switch (status) {
            case "0":
            case "PENDING":
                return 0;
            case "1":
            case "IN_PROGRESS":
                return 1;
            case "2":
            case "COMPLETED":
                return 2;
            case "3":
            case "CLOSED":
                return 3;
            default:
                return null;
        }
    }

    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (int) (Math.random() * 1000);
    }
}
