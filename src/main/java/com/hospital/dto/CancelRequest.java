package com.hospital.dto;

import lombok.Data;

/**
 * 取消预约请求DTO
 */
@Data
public class CancelRequest {

    private String cancelReason;
}
