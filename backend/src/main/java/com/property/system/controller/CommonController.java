package com.property.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.system.dto.Result;
import com.property.system.entity.*;
import com.property.system.repository.*;
import com.property.system.security.RequireRole;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommonController {

    private final CommunityMapper communityMapper;
    private final BuildingMapper buildingMapper;
    private final RoomMapper roomMapper;
    private final DeviceCategoryMapper categoryMapper;
    private final SysUserMapper userMapper;
    private final SysDictMapper dictMapper;

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/communities")
    public Result<List<Community>> communities() {
        Long tenantId = RequestContext.getTenantId();
        return Result.success(communityMapper.selectList(
                new LambdaQueryWrapper<Community>().eq(Community::getTenantId, tenantId)));
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/buildings")
    public Result<List<Building>> buildings(@RequestParam(required = false) Long communityId) {
        Long tenantId = RequestContext.getTenantId();
        return Result.success(buildingMapper.selectList(
                new LambdaQueryWrapper<Building>()
                        .eq(Building::getTenantId, tenantId)
                        .eq(communityId != null, Building::getCommunityId, communityId)));
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/rooms")
    public Result<List<Room>> rooms(@RequestParam(required = false) Long buildingId) {
        Long tenantId = RequestContext.getTenantId();
        return Result.success(roomMapper.selectList(
                new LambdaQueryWrapper<Room>()
                        .eq(Room::getTenantId, tenantId)
                        .eq(buildingId != null, Room::getBuildingId, buildingId)));
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/device-categories")
    public Result<List<DeviceCategory>> deviceCategories() {
        Long tenantId = RequestContext.getTenantId();
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<DeviceCategory>().eq(DeviceCategory::getTenantId, tenantId)));
    }

    @RequireRole({"系统管理员", "维修工"})
    @GetMapping("/workers")
    public Result<List<SysUser>> workers() {
        Long tenantId = RequestContext.getTenantId();
        List<SysUser> workers = userMapper.findByRole(tenantId, "维修工");
        return Result.success(workers);
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/dicts")
    public Result<List<SysDict>> dicts(@RequestParam String dictType) {
        Long tenantId = RequestContext.getTenantId();
        return Result.success(dictMapper.selectList(
                new LambdaQueryWrapper<SysDict>()
                        .eq(SysDict::getTenantId, tenantId)
                        .eq(SysDict::getDictType, dictType)));
    }
}
