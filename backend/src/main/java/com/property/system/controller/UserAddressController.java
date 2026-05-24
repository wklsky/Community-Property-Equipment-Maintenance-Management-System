package com.property.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.system.dto.MyAddressVO;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressMapper addressMapper;
    private final PropertyMapper propertyMapper;

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/api/v1/user-addresses")
    public Result<List<UserAddress>> list() {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();
        List<UserAddress> list = addressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getTenantId, tenantId)
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getCreateTime));
        return Result.success(list);
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @PostMapping("/api/v1/user-addresses")
    @Transactional
    public Result<UserAddress> add(@RequestBody UserAddress address) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();
        address.setTenantId(tenantId);
        address.setUserId(userId);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        address.setCreateTime(LocalDateTime.now());

        if (address.getIsDefault() == 1) {
            clearAllDefaults(tenantId, userId);
        }

        addressMapper.insert(address);
        return Result.success(address);
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @PutMapping("/api/v1/user-addresses/{id}")
    @Transactional
    public Result<Void> update(@PathVariable Long id, @RequestBody UserAddress address) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getTenantId().equals(tenantId)
                || !existing.getUserId().equals(userId)) {
            return Result.error(404, "地址不存在");
        }

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearAllDefaults(tenantId, userId);
        }

        address.setId(id);
        addressMapper.updateById(address);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @DeleteMapping("/api/v1/user-addresses/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getTenantId().equals(tenantId)
                || !existing.getUserId().equals(userId)) {
            return Result.error(404, "地址不存在");
        }

        addressMapper.deleteById(id);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @PutMapping("/api/v1/user-addresses/{id}/default")
    @Transactional
    public Result<Void> setDefault(@PathVariable Long id) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getTenantId().equals(tenantId)
                || !existing.getUserId().equals(userId)) {
            return Result.error(404, "地址不存在");
        }

        clearAllDefaults(tenantId, userId);
        existing.setIsDefault(1);
        addressMapper.updateById(existing);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/api/v1/my-addresses")
    public Result<List<MyAddressVO>> myAddresses() {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        List<MyAddressVO> result = new ArrayList<>();

        // 1. System properties
        List<MyPropertyVO> properties = propertyMapper.listMyProperties(tenantId, userId);
        for (MyPropertyVO p : properties) {
            MyAddressVO vo = new MyAddressVO();
            vo.setId(p.getId());
            vo.setType("property");
            vo.setAddress(p.getFullAddress());
            vo.setCommunityName(p.getCommunityName());
            vo.setBuildingName(p.getBuildingName());
            vo.setRoomNo(p.getRoomNo());
            vo.setIsDefault(p.getIsDefault());
            result.add(vo);
        }

        // 2. Custom addresses
        List<UserAddress> addresses = addressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getTenantId, tenantId)
                        .eq(UserAddress::getUserId, userId));
        for (UserAddress a : addresses) {
            MyAddressVO vo = new MyAddressVO();
            vo.setId(a.getId());
            vo.setType("custom");
            vo.setAddress(a.getAddress());
            vo.setIsDefault(a.getIsDefault());
            result.add(vo);
        }

        // Sort: default first, then by id
        result.sort((a, b) -> {
            if (a.getIsDefault() == 1 && b.getIsDefault() != 1) return -1;
            if (a.getIsDefault() != 1 && b.getIsDefault() == 1) return 1;
            return 0;
        });

        return Result.success(result);
    }

    private void clearAllDefaults(Long tenantId, Long userId) {
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

        // Clear property defaults
        List<Property> propDefaults = propertyMapper.selectList(
                new LambdaQueryWrapper<Property>()
                        .eq(Property::getTenantId, tenantId)
                        .eq(Property::getOwnerId, userId)
                        .eq(Property::getIsDefault, 1));
        for (Property p : propDefaults) {
            p.setIsDefault(0);
            propertyMapper.updateById(p);
        }
    }
}
