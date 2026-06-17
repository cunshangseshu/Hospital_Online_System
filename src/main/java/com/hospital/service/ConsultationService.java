package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.dto.*;
import com.hospital.entity.Consultation;

import java.util.List;

/**
 * 在线问诊服务接口
 */
public interface ConsultationService extends IService<Consultation> {

    /**
     * 发起问诊
     */
    ConsultationVO createConsultation(ConsultationRequest request);

    /**
     * 发送消息
     */
    ConsultationMessageVO sendMessage(Long consultationId, SendMessageRequest request);

    /**
     * 获取问诊详情（含消息列表和处方）
     */
    ConsultationVO getConsultationDetail(Long consultationId);

    /**
     * 获取我的问诊列表
     */
    PageResult<ConsultationVO> getMyConsultations(Long userId, String userType, String status, Integer pageNum, Integer pageSize);

    /**
     * 医生接诊
     */
    void acceptConsultation(Long consultationId);

    /**
     * 结束问诊
     */
    void finishConsultation(Long consultationId);

    /**
     * 获取问诊中的新消息（AJAX轮询）
     */
    List<ConsultationMessageVO> getNewMessages(Long consultationId, Long afterMessageId);
}
