package com.property.system.security;

import com.property.system.common.ResultCode;
import com.property.system.exception.BusinessException;
import com.property.system.repository.SysPermissionMapper;
import com.property.system.repository.SysRoleMapper;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;

    @Around("@annotation(com.property.system.security.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);

        Long userId = RequestContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String[] requiredPermissions = annotation.value();
        RequirePermission.Logical logical = annotation.logical();

        List<String> userPermissions = permissionMapper.findPermissionCodesByUserId(userId);

        boolean hasPermission;
        if (logical == RequirePermission.Logical.AND) {

            hasPermission = userPermissions.containsAll(Arrays.asList(requiredPermissions));
        } else {

            hasPermission = Arrays.stream(requiredPermissions)
                    .anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            log.warn("用户 {} 权限不足，需要权限: {}, 拥有权限: {}",
                    userId, Arrays.toString(requiredPermissions), userPermissions);
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(com.property.system.security.RequireSuperAdmin)")
    public Object checkSuperAdmin(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = RequestContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        if (!RequestContext.isSuperAdmin()) {
            log.warn("用户 {} 非超级管理员，拒绝访问", userId);
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(com.property.system.security.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole annotation = method.getAnnotation(RequireRole.class);

        Long userId = RequestContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        if (RequestContext.isSuperAdmin()) {
            return joinPoint.proceed();
        }

        String[] requiredRoles = annotation.value();
        RequirePermission.Logical logical = annotation.logical();

        List<String> userRoles = roleMapper.findRoleNamesByUserId(userId);

        boolean hasRole;
        if (logical == RequirePermission.Logical.AND) {
            hasRole = userRoles.containsAll(Arrays.asList(requiredRoles));
        } else {
            hasRole = Arrays.stream(requiredRoles)
                    .anyMatch(userRoles::contains);
        }

        if (!hasRole) {
            log.warn("用户 {} 角色不足，需要角色: {}, 拥有角色: {}",
                    userId, Arrays.toString(requiredRoles), userRoles);
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        return joinPoint.proceed();
    }
}
