package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String orderNo;
    private Long userId;
    private Long deviceId;
    private String address;
    private String faultDesc;
    private Integer status;
    private Integer priority;
    private Long assignTo;
    private LocalDateTime appointTime;
    private LocalDateTime finishTime;
    private String processDesc;
    private String resultImages;
    private String transferReason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
