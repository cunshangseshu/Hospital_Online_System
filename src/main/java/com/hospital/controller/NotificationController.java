package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.NotificationVO;
import com.hospital.service.NotificationService;
import com.hospital.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {
    @Autowired private NotificationService notificationService;

    @GetMapping("/my-list")
    public Result<List<NotificationVO>> getMyList() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return Result.error(401, "请先登录");
        return Result.success(notificationService.getByUserId(userId));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return Result.error(401, "请先登录");
        return Result.success(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @PostMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id) {
        try { notificationService.markAsRead(id); return Result.success("已标记已读", null); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @PostMapping("/read-all")
    public Result<String> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return Result.error(401, "请先登录");
        notificationService.markAllAsRead(userId);
        return Result.success("全部已读", null);
    }
}
