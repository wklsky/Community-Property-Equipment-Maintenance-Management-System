package com.property.system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RepairOrderVO {

    private Long id;
    private Long tenantId;
    private String orderNo;
    private Long userId;
    private String userName;
    private String userPhone;
    private Long deviceId;
    private String deviceName;
    private String deviceLocation;
    private String address;
    private String faultDesc;
    private Integer status;
    private String statusName;
    private Integer priority;
    private String priorityName;
    private Long assignTo;
    private String assignToName;
    private LocalDateTime appointTime;
    private LocalDateTime finishTime;
    private String processDesc;
    private String resultImages;
    private String transferReason;
    private LocalDateTime createTime;

    private Integer rating;
    private String comment;
}
