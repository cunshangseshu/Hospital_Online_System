package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.dto.NotificationVO;
import com.hospital.entity.Notification;
import java.util.List;

public interface NotificationService extends IService<Notification> {
    List<NotificationVO> getByUserId(Long userId);
    int getUnreadCount(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
}
