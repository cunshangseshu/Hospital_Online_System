package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.dto.PrescriptionRequest;
import com.hospital.dto.PrescriptionVO;
import com.hospital.entity.*;
import com.hospital.mapper.ConsultationMapper;
import com.hospital.mapper.ConsultationMessageMapper;
import com.hospital.mapper.PrescriptionItemMapper;
import com.hospital.mapper.PrescriptionMapper;
import com.hospital.security.SecurityUtils;
import com.hospital.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处方服务实现类
 */
@Service
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription> implements PrescriptionService {

    @Autowired
    private PrescriptionItemMapper itemMapper;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ConsultationMessageMapper messageMapper;

    @Autowired
    private ConsultationMapper consultationMapper;

    @Override
    @Transactional
    public PrescriptionVO createPrescription(PrescriptionRequest request) {
        Long doctorUserId = SecurityUtils.getCurrentUserId();
        if (doctorUserId == null) {
            throw new RuntimeException("请先登录");
        }

        // 通过userId查找doctorId
        Doctor doctor = doctorService.getOne(
            new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, doctorUserId)
        );
        if (doctor == null) {
            throw new RuntimeException("医生信息不存在");
        }

        // 获取问诊记录
        Consultation consultation = consultationMapper.selectById(request.getConsultationId());
        if (consultation == null) {
            throw new RuntimeException("问诊记录不存在");
        }

        // 创建处方
        Prescription prescription = new Prescription();
        prescription.setPrescriptionNo(generateNo("RX"));
        prescription.setConsultationId(request.getConsultationId());
        prescription.setDoctorId(doctor.getId());
        prescription.setPatientId(consultation.getPatientId());
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setAdvice(request.getAdvice());
        prescription.setRemark(request.getRemark());
        prescription.setStatus(1); // 直接生效
        prescription.setTotalAmount(BigDecimal.ZERO);

        save(prescription);

        // 创建处方明细
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PrescriptionVO.PrescriptionItemVO> itemVOs = new ArrayList<>();

        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {
                Medicine medicine = medicineService.getById(itemReq.getMedicineId());
                if (medicine == null) continue;

                PrescriptionItem item = new PrescriptionItem();
                item.setPrescriptionId(prescription.getId());
                item.setMedicineId(medicine.getId());
                item.setMedicineName(medicine.getMedicineName());
                item.setSpecification(medicine.getSpecification());
                item.setDosage(itemReq.getDosage());
                item.setUsageMethod(itemReq.getUsageMethod());
                item.setFrequency(itemReq.getFrequency());
                item.setDays(itemReq.getDays());
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(medicine.getPrice());
                BigDecimal subtotal = medicine.getPrice().multiply(new BigDecimal(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1));
                item.setSubtotal(subtotal);
                totalAmount = totalAmount.add(subtotal);

                itemMapper.insert(item);

                PrescriptionVO.PrescriptionItemVO itemVO = new PrescriptionVO.PrescriptionItemVO();
                itemVO.setId(item.getId());
                itemVO.setMedicineId(item.getMedicineId());
                itemVO.setMedicineName(item.getMedicineName());
                itemVO.setSpecification(item.getSpecification());
                itemVO.setDosage(item.getDosage());
                itemVO.setUsageMethod(item.getUsageMethod());
                itemVO.setFrequency(item.getFrequency());
                itemVO.setDays(item.getDays());
                itemVO.setQuantity(item.getQuantity());
                itemVO.setUnitPrice(item.getUnitPrice());
                itemVO.setSubtotal(item.getSubtotal());
                itemVOs.add(itemVO);
            }
        }

        // 更新处方总金额
        prescription.setTotalAmount(totalAmount);
        updateById(prescription);

        // 插入处方消息到问诊中
        ConsultationMessage msg = new ConsultationMessage();
        msg.setConsultationId(request.getConsultationId());
        msg.setSenderId(doctorUserId);
        msg.setSenderType("DOCTOR");
        msg.setMessageType("PRESCRIPTION");
        msg.setContent("医生开具了处方：" + prescription.getPrescriptionNo());
        msg.setRefPrescriptionId(prescription.getId());
        msg.setIsRead(0);
        messageMapper.insert(msg);

        // 构建返回VO
        PrescriptionVO vo = convertToVO(prescription);
        vo.setItems(itemVOs);
        return vo;
    }

    @Override
    public PrescriptionVO getPrescriptionDetail(Long prescriptionId) {
        Prescription prescription = getById(prescriptionId);
        if (prescription == null) {
            throw new RuntimeException("处方不存在");
        }
        return convertToVO(prescription);
    }

    @Override
    public List<PrescriptionVO> getByConsultationId(Long consultationId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getConsultationId, consultationId)
               .orderByDesc(Prescription::getCreatedAt);

        List<Prescription> prescriptions = list(wrapper);
        return prescriptions.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private PrescriptionVO convertToVO(Prescription p) {
        PrescriptionVO vo = new PrescriptionVO();
        vo.setId(p.getId());
        vo.setPrescriptionNo(p.getPrescriptionNo());
        vo.setConsultationId(p.getConsultationId());
        vo.setDiagnosis(p.getDiagnosis());
        vo.setAdvice(p.getAdvice());
        vo.setTotalAmount(p.getTotalAmount());
        vo.setStatus(p.getStatus());
        vo.setStatusText(p.getStatus() != null && p.getStatus() == 1 ? "已生效" : p.getStatus() == 0 ? "待审核" : "已作废");
        vo.setRemark(p.getRemark());
        vo.setCreatedAt(p.getCreatedAt());

        // 医生姓名
        Doctor doctor = doctorService.getById(p.getDoctorId());
        if (doctor != null) {
            User doctorUser = userService.getById(doctor.getUserId());
            if (doctorUser != null) {
                vo.setDoctorName(doctorUser.getRealName());
            }
        }

        // 患者姓名
        User patient = userService.getById(p.getPatientId());
        if (patient != null) {
            vo.setPatientName(patient.getRealName());
        }

        // 处方明细
        LambdaQueryWrapper<PrescriptionItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PrescriptionItem::getPrescriptionId, p.getId());
        List<PrescriptionItem> items = itemMapper.selectList(itemWrapper);

        List<PrescriptionVO.PrescriptionItemVO> itemVOs = items.stream().map(item -> {
            PrescriptionVO.PrescriptionItemVO itemVO = new PrescriptionVO.PrescriptionItemVO();
            itemVO.setId(item.getId());
            itemVO.setMedicineId(item.getMedicineId());
            itemVO.setMedicineName(item.getMedicineName());
            itemVO.setSpecification(item.getSpecification());
            itemVO.setDosage(item.getDosage());
            itemVO.setUsageMethod(item.getUsageMethod());
            itemVO.setFrequency(item.getFrequency());
            itemVO.setDays(item.getDays());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUnitPrice(item.getUnitPrice());
            itemVO.setSubtotal(item.getSubtotal());
            itemVO.setRemark(item.getRemark());
            return itemVO;
        }).collect(Collectors.toList());
        vo.setItems(itemVOs);

        return vo;
    }

    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (int) (Math.random() * 1000);
    }
}
