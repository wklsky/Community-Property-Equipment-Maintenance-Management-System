package com.property.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.dto.Result;
import com.property.system.entity.Device;
import com.property.system.security.RequireRole;
import com.property.system.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping
    public Result<Page<Device>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Integer status) {
        return Result.success(deviceService.page(pageNum, pageSize, categoryId, buildingId, status));
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/{id}")
    public Result<Device> getById(@PathVariable Long id) {
        return Result.success(deviceService.getById(id));
    }

    @RequireRole("系统管理员")
    @PostMapping
    public Result<Device> create(@RequestBody Device device) {
        return Result.success(deviceService.create(device));
    }

    @RequireRole("系统管理员")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Device device) {
        device.setId(id);
        deviceService.update(device);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工"})
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        deviceService.updateStatus(id, status);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return Result.success();
    }
}
