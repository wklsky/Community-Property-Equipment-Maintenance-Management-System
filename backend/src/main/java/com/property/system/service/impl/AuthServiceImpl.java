package com.property.system.service.impl;

import com.property.system.dto.*;
import com.property.system.entity.SysRole;
import com.property.system.entity.SysUser;
import com.property.system.entity.SysUserRole;
import com.property.system.exception.BusinessException;
import com.property.system.repository.SysRoleMapper;
import com.property.system.repository.SysUserMapper;
import com.property.system.repository.SysUserRoleMapper;
import com.property.system.security.JwtTokenProvider;
import com.property.system.service.AuthService;
import com.property.system.service.SmsService;
import com.property.system.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user;

        if (request.getTenantId() != null) {
            user = userMapper.selectForLogin(request.getTenantId(), request.getPhone());
        } else {
            // 超级管理员登录：绕过租户隔离查询
            TenantContextHolder.setIgnoreTenant(true);
            try {
                user = userMapper.selectByPhone(request.getPhone());
            } finally {
                TenantContextHolder.setIgnoreTenant(false);
            }
        }

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getIsSuperAdmin() != null && user.getIsSuperAdmin() == 1) {
            // super admin login — no tenantId validation needed
        } else if (request.getTenantId() == null) {
            throw new BusinessException("请选择物业公司");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("刷新Token无效或已过期，请重新登录");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException("无效的刷新Token，请使用刷新Token进行刷新");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在或已被禁用");
        }

        return buildLoginResponse(user);
    }

    @Override
    public void sendCode(SmsCodeSendRequest request) {

        SysUser user = userMapper.selectForLogin(request.getTenantId(), request.getPhone());
        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("该账号已被禁用");
        }
        smsService.generateCode(request.getPhone(), request.getTenantId());
    }

    @Override
    public LoginResponse loginByCode(SmsCodeLoginRequest request) {

        smsService.verifyCode(request.getPhone(), request.getTenantId(), request.getCode());

        SysUser user = userMapper.selectForLogin(request.getTenantId(), request.getPhone());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        return buildLoginResponse(user);
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {

        SysUser existing = userMapper.selectForLogin(request.getTenantId(), request.getPhone());
        if (existing != null) {
            throw new BusinessException("该手机号已注册");
        }

        SysRole ownerRole = roleMapper.findByTenantIdAndRoleName(
                request.getTenantId(), "业主");
        if (ownerRole == null) {
            throw new BusinessException("系统配置错误：未找到业主角色");
        }

        SysUser user = new SysUser();
        user.setTenantId(request.getTenantId());
        user.setPhone(request.getPhone());
        user.setUsername(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(ownerRole.getId());
        userRoleMapper.insert(userRole);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        smsService.verifyCode(request.getPhone(), request.getTenantId(), request.getCode());

        SysUser user = userMapper.selectForLogin(request.getTenantId(), request.getPhone());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    private LoginResponse buildLoginResponse(SysUser user) {
        boolean isSuperAdmin = user.getIsSuperAdmin() != null && user.getIsSuperAdmin() == 1;
        String roleName = isSuperAdmin ? "超级管理员" : userMapper.findRoleNameByUserId(user.getId());
        String token = jwtTokenProvider.createToken(user.getId(), user.getTenantId(), roleName, isSuperAdmin);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getTenantId());
        long expiresAt = System.currentTimeMillis() + jwtTokenProvider.getExpirationMs();

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .userId(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .tenantId(user.getTenantId())
                .roleName(roleName)
                .isSuperAdmin(isSuperAdmin)
                .build();
    }
}
