package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.dto.ImagingReportVO;
import com.hospital.entity.*;
import com.hospital.mapper.ImagingReportMapper;
import com.hospital.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImagingReportServiceImpl extends ServiceImpl<ImagingReportMapper, ImagingReport> implements ImagingReportService {
    @Autowired private DoctorService doctorService;
    @Autowired private UserService userService;

    @Override
    public List<ImagingReportVO> getByPatientId(Long patientId) {
        LambdaQueryWrapper<ImagingReport> w = new LambdaQueryWrapper<>();
        w.eq(ImagingReport::getPatientId, patientId).orderByDesc(ImagingReport::getReportDate);
        return list(w).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ImagingReportVO getReportDetail(Long id) { return toVO(getById(id)); }

    private ImagingReportVO toVO(ImagingReport r) {
        ImagingReportVO v = new ImagingReportVO();
        v.setId(r.getId()); v.setReportNo(r.getReportNo()); v.setReportName(r.getReportName());
        v.setModality(r.getModality()); v.setBodyPart(r.getBodyPart());
        v.setFinding(r.getFinding()); v.setImpression(r.getImpression());
        v.setReportDate(r.getReportDate());
        if (r.getDoctorId() != null) {
            Doctor d = doctorService.getById(r.getDoctorId());
            if (d != null) { User u = userService.getById(d.getUserId()); if (u != null) v.setDoctorName(u.getRealName()); }
        }
        return v;
    }
}
