package com.property.system.repository;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<SysRole> findByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> findRoleCodesByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT r.role_name FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);

    @Select("SELECT r.role_name FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} LIMIT 1")
    String findRoleNameByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM sys_role WHERE tenant_id = #{tenantId} AND role_name = #{roleName} LIMIT 1")
    SysRole findByTenantIdAndRoleName(@Param("tenantId") Long tenantId, @Param("roleName") String roleName);
}
