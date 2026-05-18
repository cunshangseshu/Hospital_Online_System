package com.hospital.dto;

import lombok.Data;

/**
 * 发送消息请求DTO
 */
@Data
public class SendMessageRequest {

    private String content;

    private String messageType; // TEXT, IMAGE
}
