package com.hospital.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LabReportVO {
    private Long id;
    private String reportNo;
    private String reportName;
    private String category;
    private String sampleType;
    private String result;
    private String conclusion;
    private Integer normalFlag;
    private String normalFlagText;
    private String doctorName;
    private LocalDate reportDate;
}
