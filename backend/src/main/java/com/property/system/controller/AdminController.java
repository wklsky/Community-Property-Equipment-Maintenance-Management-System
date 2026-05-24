package com.property.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.common.ResultCode;
import com.property.system.dto.Result;
import com.property.system.dto.UserVO;
import com.property.system.entity.*;
import com.property.system.exception.BusinessException;
import com.property.system.repository.*;
import com.property.system.security.RequireSuperAdmin;
import com.property.system.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@RequireSuperAdmin
public class AdminController {

    private final SysTenantMapper tenantMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/tenants")
    public Result<Page<SysTenant>> tenants(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<SysTenant> page = tenantMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysTenant>()
                        .like(name != null && !name.isEmpty(), SysTenant::getName, name)
                        .orderByDesc(SysTenant::getCreateTime));
        return Result.success(page);
    }

    @GetMapping("/tenants/{id}")
    public Result<SysTenant> getTenant(@PathVariable Long id) {
        SysTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "物业公司不存在");
        }

        return Result.success(tenant);
    }

    @PostMapping("/tenants")
    @Transactional
    public Result<Map<String, Object>> createTenant(@RequestBody Map<String, Object> body) {
        String tenantName = (String) body.get("tenantName");
        String adminUsername = (String) body.get("adminUsername");
        String adminPhone = (String) body.get("adminPhone");
        String adminPassword = (String) body.get("adminPassword");

        if (tenantName == null || tenantName.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "公司名称不能为空");
        }
        if (adminUsername == null || adminUsername.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "管理员用户名不能为空");
        }
        if (adminPhone == null || !adminPhone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "管理员手机号格式不正确");
        }
        if (adminPassword == null || adminPassword.length() < 6) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "管理员密码至少6位");
        }

        Long existingUser = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, adminPhone));
        if (existingUser > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该手机号已被注册");
        }

        SysTenant tenant = new SysTenant();
        tenant.setName(tenantName.trim());
        tenant.setStatus(1);
        tenant.setCreateTime(LocalDateTime.now());
        tenantMapper.insert(tenant);

        SysRole adminRole = new SysRole();
        adminRole.setTenantId(tenant.getId());
        adminRole.setRoleName("系统管理员");
        adminRole.setStatus(1);
        adminRole.setSortOrder(1);
        adminRole.setCreateTime(LocalDateTime.now());
        roleMapper.insert(adminRole);

        SysRole workerRole = new SysRole();
        workerRole.setTenantId(tenant.getId());
        workerRole.setRoleName("维修工");
        workerRole.setStatus(1);
        workerRole.setSortOrder(2);
        workerRole.setCreateTime(LocalDateTime.now());
        roleMapper.insert(workerRole);

        SysRole ownerRole = new SysRole();
        ownerRole.setTenantId(tenant.getId());
        ownerRole.setRoleName("业主");
        ownerRole.setStatus(1);
        ownerRole.setSortOrder(3);
        ownerRole.setCreateTime(LocalDateTime.now());
        roleMapper.insert(ownerRole);

        SysUser adminUser = new SysUser();
        adminUser.setTenantId(tenant.getId());
        adminUser.setUsername(adminUsername.trim());
        adminUser.setPhone(adminPhone);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setStatus(1);
        adminUser.setIsSuperAdmin(0);
        adminUser.setCreateTime(LocalDateTime.now());
        userMapper.insert(adminUser);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(adminUser.getId());
        userRole.setRoleId(adminRole.getId());
        userRoleMapper.insert(userRole);

        log.info("创建物业公司: id={}, name={}, adminUserId={}", tenant.getId(), tenant.getName(), adminUser.getId());

        return Result.success(Map.of(
                "tenantId", tenant.getId(),
                "tenantName", tenant.getName(),
                "adminUserId", adminUser.getId()
        ));
    }

    @PutMapping("/tenants/{id}")
    public Result<Void> updateTenant(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "物业公司不存在");
        }

        if (body.containsKey("name")) {
            String name = (String) body.get("name");
            if (name != null && !name.trim().isEmpty()) {
                tenant.setName(name.trim());
            }
        }
        tenantMapper.updateById(tenant);
        log.info("更新物业公司: id={}, name={}", id, tenant.getName());
        return Result.success();
    }

    @PutMapping("/tenants/{id}/status")
    public Result<Void> updateTenantStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "物业公司不存在");
        }
        tenant.setStatus(status);
        tenantMapper.updateById(tenant);
        log.info("更新物业公司状态: id={}, status={}", id, status);
        return Result.success();
    }

    @GetMapping("/users")
    public Result<Page<UserVO>> crossTenantUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long roleId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(tenantId != null, SysUser::getTenantId, tenantId)
                .eq(SysUser::getIsSuperAdmin, 0)
                .and(keyword != null && !keyword.isEmpty(), w -> w
                        .like(SysUser::getUsername, keyword)
                        .or()
                        .like(SysUser::getPhone, keyword))
                .apply(roleId != null,
                        "id IN (SELECT user_id FROM sys_user_role WHERE role_id = {0})", roleId)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<UserVO> voList = page.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setTenantId(user.getTenantId());
            vo.setUsername(user.getUsername());
            vo.setPhone(user.getMaskedPhone());
            vo.setStatus(user.getStatus());
            vo.setCreateTime(user.getCreateTime());

            String roleName = userMapper.findRoleNameByUserId(user.getId());
            vo.setRoleName(roleName != null ? roleName : "未分配");

            List<SysRole> roles = roleMapper.findByUserId(user.getId());
            if (!roles.isEmpty()) {
                vo.setRoleId(roles.get(0).getId());
            }

            SysTenant tenant = tenantMapper.selectById(user.getTenantId());
            if (tenant != null) {
                vo.setTenantName(tenant.getName());
            }
            return vo;
        }).collect(Collectors.toList());

        Page<UserVO> resultPage = new Page<>(pageNum, pageSize, page.getTotal());
        resultPage.setRecords(voList);
        return Result.success(resultPage);
    }

    @GetMapping("/roles")
    public Result<List<SysRole>> crossTenantRoles(@RequestParam Long tenantId) {
        return Result.success(roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getTenantId, tenantId)
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getSortOrder)));
    }
}
