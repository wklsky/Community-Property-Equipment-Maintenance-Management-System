package com.property.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.dto.Result;
import com.property.system.dto.UserRoleBrief;
import com.property.system.dto.UserVO;
import com.property.system.entity.SysRole;
import com.property.system.entity.SysUser;
import com.property.system.entity.SysUserRole;
import com.property.system.exception.BusinessException;
import com.property.system.repository.SysRoleMapper;
import com.property.system.repository.SysUserMapper;
import com.property.system.repository.SysUserRoleMapper;
import com.property.system.security.RequireRole;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @RequireRole("系统管理员")
    @GetMapping("/users")
    public Result<Page<UserVO>> users(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId) {
        Long tenantId = RequestContext.getTenantId();

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .and(keyword != null && !keyword.isEmpty(), w -> w
                        .like(SysUser::getUsername, keyword)
                        .or()
                        .like(SysUser::getPhone, keyword))
                .apply(roleId != null,
                        "id IN (SELECT user_id FROM sys_user_role WHERE role_id = {0})", roleId)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<SysUser> records = page.getRecords();
        Map<Long, UserRoleBrief> roleBriefMap = loadRoleBriefMap(records);

        List<UserVO> voList = records.stream()
                .map(user -> toUserVO(user, roleBriefMap.get(user.getId())))
                .collect(Collectors.toList());

        Page<UserVO> resultPage = new Page<>(pageNum, pageSize, page.getTotal());
        resultPage.setRecords(voList);
        return Result.success(resultPage);
    }

    /**
     * 批量加载用户的角色信息并在内存中按 userId 建立索引。
     *
     * 优化背景：原实现在分页结果上逐条调用 findRoleNameByUserId() 与 findByUserId()，
     * 每页 10 条即产生 21 次 SQL，pageSize 调大后呈线性放大；改为一次 IN 查询后固定为 1 次。
     *
     * 多角色用户保留关联记录 id 最小的一条，与原有「取第一个角色」的对外表现保持一致。
     */
    private Map<Long, UserRoleBrief> loadRoleBriefMap(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> userIds = users.stream()
                .map(SysUser::getId)
                .collect(Collectors.toList());

        Map<Long, UserRoleBrief> briefMap = new HashMap<>();
        for (UserRoleBrief brief : userMapper.findRoleBriefByUserIds(userIds)) {
            briefMap.putIfAbsent(brief.getUserId(), brief);
        }
        return briefMap;
    }

    @RequireRole("系统管理员")
    @GetMapping("/users/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Map<Long, UserRoleBrief> roleBriefMap = loadRoleBriefMap(Collections.singletonList(user));
        return Result.success(toUserVO(user, roleBriefMap.get(user.getId())));
    }

    private UserVO toUserVO(SysUser user, UserRoleBrief brief) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setTenantId(user.getTenantId());
        vo.setUsername(user.getUsername());
        // 对外统一返回脱敏手机号，原始号码仅用于登录校验，不下发给前端
        vo.setPhone(user.getMaskedPhone());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setRoleName(brief != null ? brief.getRoleName() : "未分配");
        if (brief != null) {
            vo.setRoleId(brief.getRoleId());
        }
        return vo;
    }

    // 同时写入 sys_user 与 sys_user_role，任一失败会留下无角色的孤立账号，必须整体回滚
    @RequireRole("系统管理员")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/users")
    public Result<Void> createUser(@RequestBody Map<String, Object> body) {
        Long tenantId;
        if (RequestContext.isSuperAdmin() && body.containsKey("tenantId")) {
            Object tid = body.get("tenantId");
            tenantId = tid != null ? Long.valueOf(tid.toString()) : RequestContext.getTenantId();
        } else {
            tenantId = RequestContext.getTenantId();
        }
        String username = (String) body.get("username");
        String phone = (String) body.get("phone");
        String password = (String) body.get("password");
        Object roleIdObj = body.get("roleId");

        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码至少6位");
        }
        if (roleIdObj == null) {
            throw new BusinessException("请选择角色");
        }

        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getPhone, phone));
        if (count > 0) {
            throw new BusinessException("该手机号已被注册");
        }

        SysUser user = new SysUser();
        user.setTenantId(tenantId);
        user.setUsername(username.trim());
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(Long.valueOf(roleIdObj.toString()));
        userRoleMapper.insert(userRole);

        return Result.success();
    }

    // 更新用户主体 + 先删后插角色关联，中途失败会导致账号丢失角色，需整体回滚
    @RequireRole("系统管理员")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/users/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (body.containsKey("username")) {
            String username = (String) body.get("username");
            if (username != null && !username.trim().isEmpty()) {
                user.setUsername(username.trim());
            }
        }
        if (body.containsKey("password")) {
            String password = (String) body.get("password");
            if (password != null && !password.isEmpty()) {
                if (password.length() < 6) {
                    throw new BusinessException("密码至少6位");
                }
                user.setPassword(passwordEncoder.encode(password));
            }
        }
        userMapper.updateById(user);

        if (body.containsKey("roleId")) {
            Object roleIdObj = body.get("roleId");
            if (roleIdObj != null) {
                Long roleId = Long.valueOf(roleIdObj.toString());
                userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, id));
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(id);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        return Result.success();
    }

    @RequireRole("系统管理员")
    @PutMapping("/users/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @GetMapping("/roles")
    public Result<List<SysRole>> roles() {
        Long tenantId = RequestContext.getTenantId();
        return Result.success(roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getTenantId, tenantId)
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getSortOrder)));
    }
}
