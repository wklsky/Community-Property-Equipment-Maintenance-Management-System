package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_dict")
public class SysDict {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String dictType;
    private String dictKey;
    private String dictValue;
}
