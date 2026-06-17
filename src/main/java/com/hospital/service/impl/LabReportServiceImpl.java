package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.dto.LabReportVO;
import com.hospital.entity.*;
import com.hospital.mapper.LabReportMapper;
import com.hospital.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabReportServiceImpl extends ServiceImpl<LabReportMapper, LabReport> implements LabReportService {
    @Autowired private DoctorService doctorService;
    @Autowired private UserService userService;

    @Override
    public List<LabReportVO> getByPatientId(Long patientId) {
        LambdaQueryWrapper<LabReport> w = new LambdaQueryWrapper<>();
        w.eq(LabReport::getPatientId, patientId).orderByDesc(LabReport::getReportDate);
        return list(w).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public LabReportVO getReportDetail(Long id) { return toVO(getById(id)); }

    private LabReportVO toVO(LabReport r) {
        LabReportVO v = new LabReportVO();
        v.setId(r.getId()); v.setReportNo(r.getReportNo()); v.setReportName(r.getReportName());
        v.setCategory(r.getCategory()); v.setSampleType(r.getSampleType());
        v.setResult(r.getResult()); v.setConclusion(r.getConclusion());
        v.setNormalFlag(r.getNormalFlag());
        v.setNormalFlagText(r.getNormalFlag() != null && r.getNormalFlag() == 1 ? "正常" : "异常");
        v.setReportDate(r.getReportDate());
        if (r.getDoctorId() != null) {
            Doctor d = doctorService.getById(r.getDoctorId());
            if (d != null) { User u = userService.getById(d.getUserId()); if (u != null) v.setDoctorName(u.getRealName()); }
        }
        return v;
    }
}
