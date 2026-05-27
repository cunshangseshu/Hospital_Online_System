package com.hospital.dto;

import lombok.Data;

@Data
public class ComplaintRequest {
    private String type;
    private String title;
    private String content;
    private String contact;
}
