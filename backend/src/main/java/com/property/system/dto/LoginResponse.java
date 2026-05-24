package com.property.system.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;

    private String refreshToken;

    private Long expiresAt;
    private Long userId;
    private String username;
    private String phone;
    private Long tenantId;
    private String roleName;
    private Boolean isSuperAdmin;
}
