package com.hospital.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private String typeText;
    private Integer isRead;
    private Long refId;
    private String time;
    private LocalDateTime createdAt;
}
