package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inspection_task")
public class InspectionTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long planId;
    private String planName;
    private Long buildingId;
    private Long categoryId;
    private Long deviceId;
    private Long assignedTo;
    private LocalDate taskDate;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
