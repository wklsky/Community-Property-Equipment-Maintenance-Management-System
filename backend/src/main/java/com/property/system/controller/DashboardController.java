package com.property.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.system.dto.DashboardVO;
import com.property.system.dto.Result;
import com.property.system.entity.Device;
import com.property.system.entity.InspectionTask;
import com.property.system.entity.RepairOrder;
import com.property.system.repository.DeviceMapper;
import com.property.system.repository.InspectionTaskMapper;
import com.property.system.repository.RepairOrderMapper;
import com.property.system.security.RequireRole;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final RepairOrderMapper orderMapper;
    private final DeviceMapper deviceMapper;
    private final InspectionTaskMapper taskMapper;

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/stats")
    public Result<DashboardVO> stats() {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();
        String role = RequestContext.getRole();

        Long filterByUserId = null;
        Long filterByWorker = null;
        if ("业主".equals(role)) {
            filterByUserId = userId;
        } else if ("维修工".equals(role)) {
            filterByWorker = userId;
        }

        DashboardVO.OrderStats orderStats = DashboardVO.OrderStats.builder()
                .pendingAccept(countOrders(tenantId, 0, filterByUserId, filterByWorker))
                .pendingAssign(countOrders(tenantId, 1, filterByUserId, filterByWorker))
                .pending(countOrders(tenantId, 2, filterByUserId, filterByWorker))
                .processing(countOrders(tenantId, 3, filterByUserId, filterByWorker))
                .pendingEvaluate(countOrders(tenantId, 4, filterByUserId, filterByWorker))
                .completed(countOrders(tenantId, 5, filterByUserId, filterByWorker))
                .cancelled(countOrders(tenantId, 6, filterByUserId, filterByWorker))
                .total(countOrders(tenantId, null, filterByUserId, filterByWorker))
                .build();

        DashboardVO.DeviceStats deviceStats = DashboardVO.DeviceStats.builder()
                .normal(countDevices(tenantId, 1))
                .faulty(countDevices(tenantId, 2))
                .repairing(countDevices(tenantId, 3))
                .disabled(countDevices(tenantId, 4))
                .total(countDevices(tenantId, null))
                .build();

        LambdaQueryWrapper<InspectionTask> taskWrapper = new LambdaQueryWrapper<InspectionTask>()
                .eq(InspectionTask::getTenantId, tenantId)
                .eq(InspectionTask::getStatus, 2)
                .eq(InspectionTask::getTaskDate, LocalDate.now());
        if (filterByWorker != null) {
            taskWrapper.eq(InspectionTask::getAssignedTo, filterByWorker);
        }
        long completedToday = taskMapper.selectCount(taskWrapper);

        DashboardVO.InspectionStats inspectionStats = DashboardVO.InspectionStats.builder()
                .pending(countTasks(tenantId, 0, filterByWorker))
                .processing(countTasks(tenantId, 1, filterByWorker))
                .completedToday(completedToday)
                .total(countTasks(tenantId, null, filterByWorker))
                .build();

        LambdaQueryWrapper<RepairOrder> recentWrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getTenantId, tenantId)
                .orderByDesc(RepairOrder::getCreateTime)
                .last("LIMIT 5");
        if (filterByUserId != null) {
            recentWrapper.eq(RepairOrder::getUserId, filterByUserId);
        } else if (filterByWorker != null) {
            recentWrapper.eq(RepairOrder::getAssignTo, filterByWorker);
        }

        List<DashboardVO.RecentOrder> recentOrders = orderMapper.selectList(recentWrapper)
                .stream()
                .map(order -> DashboardVO.RecentOrder.builder()
                        .id(order.getId())
                        .orderNo(order.getOrderNo())
                        .faultDesc(order.getFaultDesc())
                        .address(order.getAddress())
                        .status(order.getStatus())
                        .statusName(getStatusName(order.getStatus()))
                        .createTime(order.getCreateTime() != null ?
                                order.getCreateTime().toString() : null)
                        .build())
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29);
        String startDate = thirtyDaysAgo.format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<DashboardVO.OrderTrendItem> rawTrend = orderMapper.selectOrderTrend(
                tenantId, startDate, filterByUserId, filterByWorker);
        Map<String, DashboardVO.OrderTrendItem> trendMap = new LinkedHashMap<>();
        for (DashboardVO.OrderTrendItem item : rawTrend) {
            trendMap.put(item.getDate(), item);
        }
        List<DashboardVO.OrderTrendItem> orderTrend = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            String date = thirtyDaysAgo.plusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);
            DashboardVO.OrderTrendItem existing = trendMap.get(date);
            if (existing != null) {
                orderTrend.add(existing);
            } else {
                orderTrend.add(DashboardVO.OrderTrendItem.builder()
                        .date(date)
                        .count(0L)
                        .completed(0L)
                        .build());
            }
        }

        DashboardVO dashboard = DashboardVO.builder()
                .orderStats(orderStats)
                .deviceStats(deviceStats)
                .inspectionStats(inspectionStats)
                .orderTrend(orderTrend)
                .recentOrders(recentOrders)
                .build();

        return Result.success(dashboard);
    }

    private long countOrders(Long tenantId, Integer status, Long userId, Long workerId) {
        return orderMapper.selectCount(
                new LambdaQueryWrapper<RepairOrder>()
                        .eq(RepairOrder::getTenantId, tenantId)
                        .eq(status != null, RepairOrder::getStatus, status)
                        .eq(userId != null, RepairOrder::getUserId, userId)
                        .eq(workerId != null, RepairOrder::getAssignTo, workerId));
    }

    private long countDevices(Long tenantId, Integer status) {
        return deviceMapper.selectCount(
                new LambdaQueryWrapper<Device>()
                        .eq(Device::getTenantId, tenantId)
                        .eq(status != null, Device::getStatus, status));
    }

    private long countTasks(Long tenantId, Integer status, Long workerId) {
        return taskMapper.selectCount(
                new LambdaQueryWrapper<InspectionTask>()
                        .eq(InspectionTask::getTenantId, tenantId)
                        .eq(status != null, InspectionTask::getStatus, status)
                        .eq(workerId != null, InspectionTask::getAssignedTo, workerId));
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "待受理";
            case 1: return "待派单";
            case 2: return "待处理";
            case 3: return "处理中";
            case 4: return "待评价";
            case 5: return "已完成";
            case 6: return "已取消";
            default: return "未知";
        }
    }
}
