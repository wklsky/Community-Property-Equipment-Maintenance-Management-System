package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("building")
public class Building {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long communityId;
    private String name;
}
