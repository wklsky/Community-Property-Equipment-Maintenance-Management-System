package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("repair_order_flow")
public class RepairOrderFlow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Integer fromStatus;
    private Integer toStatus;
    private String action;
}
