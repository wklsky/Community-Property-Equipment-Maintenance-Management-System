package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_order_evaluation")
public class RepairOrderEvaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long orderId;
    private Integer rating;
    private String comment;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
