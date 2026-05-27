package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.dto.ComplaintRequest;
import com.hospital.entity.Complaint;
import java.util.List;

public interface ComplaintService extends IService<Complaint> {
    Complaint submit(ComplaintRequest request);
    List<Complaint> getByUserId(Long userId);
}
