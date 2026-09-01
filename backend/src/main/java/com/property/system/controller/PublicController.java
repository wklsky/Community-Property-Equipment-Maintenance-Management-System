package com.property.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.system.dto.Result;
import com.property.system.dto.TenantOptionVO;
import com.property.system.entity.SysTenant;
import com.property.system.repository.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final SysTenantMapper tenantMapper;

    /**
     * 登录页的租户下拉数据源。
     * 免认证接口，只返回 id 与 name：status/createTime 属于内部运维字段，对外暴露无业务价值。
     * 同时仅取启用状态的租户，避免已停用的公司出现在登录选项中。
     */
    @GetMapping("/tenants")
    public Result<List<TenantOptionVO>> tenants() {
        List<SysTenant> tenants = tenantMapper.selectList(
                new LambdaQueryWrapper<SysTenant>()
                        .eq(SysTenant::getStatus, 1)
                        .orderByAsc(SysTenant::getId));

        List<TenantOptionVO> options = tenants.stream()
                .map(tenant -> new TenantOptionVO(tenant.getId(), tenant.getName()))
                .collect(Collectors.toList());

        return Result.success(options);
    }
}
