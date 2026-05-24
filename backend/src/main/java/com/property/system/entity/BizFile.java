package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_file")
public class BizFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String bizType;
    private Long bizId;
    private String fileType;
    private String url;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
