package com.property.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.entity.Device;
import com.property.system.exception.BusinessException;
import com.property.system.repository.DeviceMapper;
import com.property.system.service.DeviceService;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceMapper deviceMapper;

    @Override
    public Page<Device> page(Integer pageNum, Integer pageSize, Long categoryId, Long buildingId, Integer status) {
        Long tenantId = RequestContext.getTenantId();

        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<Device>()
                .eq(Device::getTenantId, tenantId)
                .eq(categoryId != null, Device::getCategoryId, categoryId)
                .eq(buildingId != null, Device::getBuildingId, buildingId)
                .eq(status != null, Device::getStatus, status)
                .orderByDesc(Device::getCreateTime);

        return deviceMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Device getById(Long id) {
        return deviceMapper.selectById(id);
    }

    @Override
    public Device create(Device device) {
        device.setTenantId(RequestContext.getTenantId());
        device.setStatus(1);
        device.setCreateTime(LocalDateTime.now());
        deviceMapper.insert(device);

        device.setQrCodeUrl("/api/v1/qrcode/device/" + device.getId());
        deviceMapper.updateById(device);

        return device;
    }

    @Override
    public void update(Device device) {
        deviceMapper.updateById(device);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        device.setStatus(status);
        deviceMapper.updateById(device);
    }

    @Override
    public void delete(Long id) {
        deviceMapper.deleteById(id);
    }
}
