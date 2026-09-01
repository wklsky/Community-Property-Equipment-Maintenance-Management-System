package com.property.system.dto;

import lombok.Data;

/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 10:40
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 10:40
 * @FilePath: backend/src/main/java/com/property/system/dto/UserRoleBrief.java
 * @Description: 用户与角色的关联简要信息，承载批量查询用户角色的结果
 */

/**
 * 查询主体是 sys_user_role 关联表，无法直接用 SysUser/SysRole 实体承接，
 * 单独定义以避免用 Map<String, Object> 承接导致的类型不安全
 */
@Data
public class UserRoleBrief {

    private Long userId;

    private Long roleId;

    private String roleName;
}
