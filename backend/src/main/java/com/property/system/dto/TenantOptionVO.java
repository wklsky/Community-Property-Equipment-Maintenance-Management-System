package com.property.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 11:08
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 11:08
 * @FilePath: backend/src/main/java/com/property/system/dto/TenantOptionVO.java
 * @Description: 登录页租户下拉选项，仅暴露选择所需的字段
 */

/**
 * /api/v1/public/tenants 是免认证接口，任何人都能调用。
 * 原先直接返回 SysTenant 实体，会把 status、createTime 等内部字段一并暴露，
 * 且后续给租户表新增字段时会无意识地扩大公开面，因此固定为最小字段集。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantOptionVO {

    private Long id;

    private String name;
}
