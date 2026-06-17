package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.dto.LabReportVO;
import com.hospital.entity.LabReport;
import java.util.List;

public interface LabReportService extends IService<LabReport> {
    List<LabReportVO> getByPatientId(Long patientId);
    LabReportVO getReportDetail(Long id);
}
