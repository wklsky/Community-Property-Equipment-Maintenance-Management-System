package com.property.system.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InspectionTaskVO {

    private Long id;
    private Long tenantId;
    private Long planId;
    private String planName;
    private Long buildingId;
    private String buildingName;
    private Long categoryId;
    private String categoryName;
    private Long deviceId;
    private String deviceName;
    private String deviceLocation;
    private Long assignedTo;
    private String assignedToName;
    private LocalDate taskDate;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
