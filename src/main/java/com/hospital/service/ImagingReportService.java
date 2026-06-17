package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.dto.ImagingReportVO;
import com.hospital.entity.ImagingReport;
import java.util.List;

public interface ImagingReportService extends IService<ImagingReport> {
    List<ImagingReportVO> getByPatientId(Long patientId);
    ImagingReportVO getReportDetail(Long id);
}
