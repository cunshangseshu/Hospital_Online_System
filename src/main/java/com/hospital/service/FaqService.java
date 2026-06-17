package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.entity.Faq;
import java.util.List;

public interface FaqService extends IService<Faq> {
    List<Faq> getByCategory(String category);
    List<Faq> searchFaqs(String keyword);
}
