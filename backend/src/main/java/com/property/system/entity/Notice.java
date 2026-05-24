package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notice")
public class Notice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String title;
    private String content;
    private Integer publishStatus;
    private LocalDateTime scheduledTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
