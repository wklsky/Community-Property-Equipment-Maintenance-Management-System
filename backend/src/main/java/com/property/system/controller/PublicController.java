package com.property.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.system.dto.Result;
import com.property.system.entity.SysTenant;
import com.property.system.repository.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final SysTenantMapper tenantMapper;

    @GetMapping("/tenants")
    public Result<List<SysTenant>> tenants() {
        return Result.success(tenantMapper.selectList(
                new LambdaQueryWrapper<SysTenant>().eq(SysTenant::getStatus, 1)));
    }
}
