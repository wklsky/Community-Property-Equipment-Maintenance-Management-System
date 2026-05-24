package com.property.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String username;

    @JsonIgnore
    private String phone;

    @JsonIgnore
    private String password;
    private Integer status;
    private Integer isSuperAdmin;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonProperty("phone")
    public String getMaskedPhone() {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
