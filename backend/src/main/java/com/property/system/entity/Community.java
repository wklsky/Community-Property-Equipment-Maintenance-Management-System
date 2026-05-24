package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("community")
public class Community {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String name;
}
