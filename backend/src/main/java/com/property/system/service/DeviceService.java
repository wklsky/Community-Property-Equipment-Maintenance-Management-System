package com.property.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.entity.Device;

public interface DeviceService {
    Page<Device> page(Integer pageNum, Integer pageSize, Long categoryId, Long buildingId, Integer status);
    Device getById(Long id);
    Device create(Device device);
    void update(Device device);
    void updateStatus(Long id, Integer status);
    void delete(Long id);
}
