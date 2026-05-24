package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_order_material")
public class RepairOrderMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long orderId;
    private String materialName;
    private Integer quantity;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
