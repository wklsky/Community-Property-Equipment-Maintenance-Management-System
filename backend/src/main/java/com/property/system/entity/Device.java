package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("device")
public class Device {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long buildingId;
    private Long categoryId;
    private String name;
    private String model;
    private LocalDate installDate;
    private String location;
    private Integer status;
    private String qrCodeUrl;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
