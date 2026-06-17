package com.hospital.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ImagingReportVO {
    private Long id;
    private String reportNo;
    private String reportName;
    private String modality;
    private String bodyPart;
    private String finding;
    private String impression;
    private String doctorName;
    private LocalDate reportDate;
}
