package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("property")
public class Property {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long roomId;
    private Long ownerId;
    private Integer isDefault;
}
