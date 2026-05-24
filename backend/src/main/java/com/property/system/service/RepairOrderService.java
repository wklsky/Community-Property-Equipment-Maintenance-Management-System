package com.property.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.dto.RepairOrderCreateRequest;
import com.property.system.dto.RepairOrderVO;
import com.property.system.entity.RepairOrder;

import java.util.List;

public interface RepairOrderService {
    RepairOrder create(RepairOrderCreateRequest request);
    Page<RepairOrder> page(Integer pageNum, Integer pageSize, Integer status, Integer priority, String orderNo, Long buildingId, String startDate, String endDate);
    Page<RepairOrder> myOrders(Integer pageNum, Integer pageSize, List<Integer> statuses);
    Page<RepairOrder> assignedOrders(Integer pageNum, Integer pageSize, List<Integer> statuses);
    RepairOrder getById(Long id);
    RepairOrderVO getDetailById(Long id);
    void assign(Long orderId, Long workerId);
    void accept(Long orderId);
    void complete(Long orderId, String processDesc);
    void evaluate(Long orderId, Integer rating, String comment);
    void cancel(Long orderId);
    void approve(Long orderId);
    void reject(Long orderId, String reason);
    void transfer(Long orderId, Long newWorkerId, String reason);
}
