package com.property.system.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DeviceVO {

    private Long id;
    private Long tenantId;
    private Long buildingId;
    private String buildingName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String model;
    private LocalDate installDate;
    private String location;
    private Integer status;
    private String statusName;
    private String qrCodeUrl;
    private LocalDateTime createTime;
}
