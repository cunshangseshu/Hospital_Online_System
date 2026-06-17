package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.dto.ComplaintRequest;
import com.hospital.entity.Complaint;
import com.hospital.mapper.ComplaintMapper;
import com.hospital.security.SecurityUtils;
import com.hospital.service.ComplaintService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint> implements ComplaintService {
    @Override
    public Complaint submit(ComplaintRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new RuntimeException("请先登录");
        Complaint c = new Complaint();
        c.setUserId(userId); c.setType(request.getType());
        c.setTitle(request.getTitle()); c.setContent(request.getContent());
        c.setContact(request.getContact()); c.setStatus(0);
        save(c);
        return c;
    }

    @Override
    public List<Complaint> getByUserId(Long userId) {
        LambdaQueryWrapper<Complaint> w = new LambdaQueryWrapper<>();
        w.eq(Complaint::getUserId, userId).orderByDesc(Complaint::getCreatedAt);
        return list(w);
    }
}
