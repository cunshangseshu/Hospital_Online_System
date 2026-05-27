package com.hospital.dto;

import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private Integer gender;
}
