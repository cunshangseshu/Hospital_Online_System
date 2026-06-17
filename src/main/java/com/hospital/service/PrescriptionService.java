package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.dto.PrescriptionRequest;
import com.hospital.dto.PrescriptionVO;
import com.hospital.entity.Prescription;

/**
 * 处方服务接口
 */
public interface PrescriptionService extends IService<Prescription> {

    /**
     * 医生开具处方
     */
    PrescriptionVO createPrescription(PrescriptionRequest request);

    /**
     * 获取处方详情
     */
    PrescriptionVO getPrescriptionDetail(Long prescriptionId);

    /**
     * 获取问诊关联的所有处方
     */
    java.util.List<PrescriptionVO> getByConsultationId(Long consultationId);
}
