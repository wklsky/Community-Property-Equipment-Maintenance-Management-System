package com.property.system.repository;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.system.dto.UserRoleBrief;
import com.property.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM sys_user WHERE tenant_id = #{tenantId} AND phone = #{phone} AND status = 1 LIMIT 1")
    SysUser selectForLogin(@Param("tenantId") Long tenantId, @Param("phone") String phone);

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND status = 1 LIMIT 1")
    SysUser selectByPhone(@Param("phone") String phone);

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT r.role_name FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} LIMIT 1")
    String findRoleNameByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT r.role_name FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);

    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE u.tenant_id = #{tenantId} AND r.role_name = #{roleName} AND u.status = 1")
    List<SysUser> findByRole(@Param("tenantId") Long tenantId, @Param("roleName") String roleName);

    /**
     * 批量查询用户角色，替代逐条调用 findRoleNameByUserId + findByUserId 造成的 N+1。
     * sys_user_role 无 tenant_id 列，租户插件拼接条件会导致 SQL 报错，故必须忽略租户拦截。
     * 调用方需保证 userIds 非空，否则 foreach 会生成非法的 IN ()。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select({"<script>",
            "SELECT ur.user_id AS user_id, ur.role_id AS role_id, r.role_name AS role_name ",
            "FROM sys_user_role ur ",
            "INNER JOIN sys_role r ON ur.role_id = r.id ",
            "WHERE ur.user_id IN ",
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> ",
            "ORDER BY ur.id ASC",
            "</script>"})
    List<UserRoleBrief> findRoleBriefByUserIds(@Param("userIds") List<Long> userIds);
}