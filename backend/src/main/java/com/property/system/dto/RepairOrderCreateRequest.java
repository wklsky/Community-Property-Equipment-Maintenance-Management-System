package com.property.system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RepairOrderCreateRequest {

    private Long deviceId;

    @NotBlank(message = "地址不能为空")
    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;

    @NotBlank(message = "故障描述不能为空")
    @Size(max = 500, message = "故障描述长度不能超过500个字符")
    private String faultDesc;

    @Min(value = 0, message = "优先级值无效")
    @Max(value = 1, message = "优先级值无效")
    private Integer priority;

    private String appointTime;
}
