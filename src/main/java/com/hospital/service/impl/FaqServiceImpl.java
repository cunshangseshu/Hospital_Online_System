package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.entity.Faq;
import com.hospital.mapper.FaqMapper;
import com.hospital.service.FaqService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class FaqServiceImpl extends ServiceImpl<FaqMapper, Faq> implements FaqService {
    @Override
    public List<Faq> getByCategory(String category) {
        LambdaQueryWrapper<Faq> w = new LambdaQueryWrapper<>();
        w.eq(Faq::getStatus, 1);
        if (StringUtils.hasText(category)) w.eq(Faq::getCategory, category);
        w.orderByAsc(Faq::getSortOrder);
        return list(w);
    }

    @Override
    public List<Faq> searchFaqs(String keyword) {
        LambdaQueryWrapper<Faq> w = new LambdaQueryWrapper<>();
        w.eq(Faq::getStatus, 1)
         .and(wr -> wr.like(Faq::getQuestion, keyword).or().like(Faq::getAnswer, keyword));
        return list(w);
    }
}
