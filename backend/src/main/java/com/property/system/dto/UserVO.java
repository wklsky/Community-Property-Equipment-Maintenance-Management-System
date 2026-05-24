package com.property.system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private String username;
    private String phone;
    private Integer status;
    private String roleName;
    private Long roleId;
    private LocalDateTime createTime;
}
