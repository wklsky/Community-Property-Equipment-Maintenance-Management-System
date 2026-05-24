package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("notice_target")
public class NoticeTarget {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long noticeId;
    private Long buildingId;
}
