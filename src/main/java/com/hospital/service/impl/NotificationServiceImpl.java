package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.dto.NotificationVO;
import com.hospital.entity.Notification;
import com.hospital.mapper.NotificationMapper;
import com.hospital.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    private static final Map<String, String> TYPE_MAP = Map.of(
        "APPOINTMENT","预约通知","CONSULTATION","问诊通知","SYSTEM","系统消息","REMINDER","就诊提醒"
    );

    @Override
    public List<NotificationVO> getByUserId(Long userId) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        w.eq(Notification::getUserId, userId).orderByDesc(Notification::getCreatedAt);
        return list(w).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        w.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        return Math.toIntExact(count(w));
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        Notification n = getById(id);
        if (n != null) { n.setIsRead(1); updateById(n); }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        w.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        List<Notification> list = list(w);
        list.forEach(n -> n.setIsRead(1));
        updateBatchById(list);
    }

    private NotificationVO toVO(Notification n) {
        NotificationVO v = new NotificationVO();
        v.setId(n.getId()); v.setTitle(n.getTitle()); v.setContent(n.getContent());
        v.setType(n.getType()); v.setTypeText(TYPE_MAP.getOrDefault(n.getType(), n.getType()));
        v.setIsRead(n.getIsRead()); v.setRefId(n.getRefId()); v.setCreatedAt(n.getCreatedAt());
        if (n.getCreatedAt() != null) v.setTime(n.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
        return v;
    }
}
