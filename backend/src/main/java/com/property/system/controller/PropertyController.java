package com.property.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.system.dto.MyPropertyVO;
import com.property.system.dto.Result;
import com.property.system.entity.Property;
import com.property.system.entity.UserAddress;
import com.property.system.repository.PropertyMapper;
import com.property.system.repository.UserAddressMapper;
import com.property.system.security.RequireRole;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyMapper propertyMapper;
    private final UserAddressMapper addressMapper;

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/my-properties")
    public Result<List<MyPropertyVO>> list() {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();
        List<MyPropertyVO> list = propertyMapper.listMyProperties(tenantId, userId);
        return Result.success(list);
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @PutMapping("/my-properties/{id}/default")
    @Transactional
    public Result<Void> setDefault(@PathVariable Long id) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        Property existing = propertyMapper.selectById(id);
        if (existing == null || !existing.getTenantId().equals(tenantId)
                || !existing.getOwnerId().equals(userId)) {
            return Result.error(404, "房产不存在");
        }

        // Clear property defaults
        List<Property> defaults = propertyMapper.selectList(
                new LambdaQueryWrapper<Property>()
                        .eq(Property::getTenantId, tenantId)
                        .eq(Property::getOwnerId, userId)
                        .eq(Property::getIsDefault, 1));
        for (Property p : defaults) {
            p.setIsDefault(0);
            propertyMapper.updateById(p);
        }

        // Clear user_address defaults
        List<UserAddress> addrDefaults = addressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getTenantId, tenantId)
                        .eq(UserAddress::getUserId, userId)
                        .eq(UserAddress::getIsDefault, 1));
        for (UserAddress a : addrDefaults) {
            a.setIsDefault(0);
            addressMapper.updateById(a);
        }

        existing.setIsDefault(1);
        propertyMapper.updateById(existing);
        return Result.success();
    }
}
