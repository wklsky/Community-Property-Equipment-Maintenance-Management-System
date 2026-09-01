package com.property.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.common.RepairOrderStatus;
import com.property.system.dto.DashboardVO;
import com.property.system.dto.Result;
import com.property.system.dto.StatusCountDTO;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    // 看板「最近工单」展示条数
    private static final int RECENT_ORDER_LIMIT = 5;

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

        // 各状态数量改为一次 GROUP BY 取回，缺失状态补 0；
        // 原先 7 个状态各自 selectCount + 1 次总数统计，共 8 条 SQL
        Map<Integer, Long> orderCountMap = toCountMap(
                orderMapper.countGroupByStatus(tenantId, filterByUserId, filterByWorker));

        DashboardVO.OrderStats orderStats = DashboardVO.OrderStats.builder()
                .pendingAccept(countOf(orderCountMap, 0))
                .pendingAssign(countOf(orderCountMap, 1))
                .pending(countOf(orderCountMap, 2))
                .processing(countOf(orderCountMap, 3))
                .pendingEvaluate(countOf(orderCountMap, 4))
                .completed(countOf(orderCountMap, 5))
                .cancelled(countOf(orderCountMap, 6))
                .total(orderCountMap.values().stream().mapToLong(Long::longValue).sum())
                .build();

        Map<Integer, Long> deviceCountMap = toCountMap(deviceMapper.countGroupByStatus(tenantId));

        DashboardVO.DeviceStats deviceStats = DashboardVO.DeviceStats.builder()
                .normal(countOf(deviceCountMap, 1))
                .faulty(countOf(deviceCountMap, 2))
                .repairing(countOf(deviceCountMap, 3))
                .disabled(countOf(deviceCountMap, 4))
                .total(deviceCountMap.values().stream().mapToLong(Long::longValue).sum())
                .build();

        LambdaQueryWrapper<InspectionTask> taskWrapper = new LambdaQueryWrapper<InspectionTask>()
                .eq(InspectionTask::getTenantId, tenantId)
                .eq(InspectionTask::getStatus, 2)
                .eq(InspectionTask::getTaskDate, LocalDate.now());
        if (filterByWorker != null) {
            taskWrapper.eq(InspectionTask::getAssignedTo, filterByWorker);
        }
        long completedToday = taskMapper.selectCount(taskWrapper);

        Map<Integer, Long> taskCountMap = toCountMap(taskMapper.countGroupByStatus(tenantId, filterByWorker));

        DashboardVO.InspectionStats inspectionStats = DashboardVO.InspectionStats.builder()
                .pending(countOf(taskCountMap, 0))
                .processing(countOf(taskCountMap, 1))
                .completedToday(completedToday)
                .total(taskCountMap.values().stream().mapToLong(Long::longValue).sum())
                .build();

        LambdaQueryWrapper<RepairOrder> recentWrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getTenantId, tenantId)
                .orderByDesc(RepairOrder::getCreateTime);
        if (filterByUserId != null) {
            recentWrapper.eq(RepairOrder::getUserId, filterByUserId);
        } else if (filterByWorker != null) {
            recentWrapper.eq(RepairOrder::getAssignTo, filterByWorker);
        }

        // 用分页插件取前 5 条，替代 last("LIMIT 5") 拼接裸 SQL 片段
        Page<RepairOrder> recentPage = orderMapper.selectPage(new Page<>(1, RECENT_ORDER_LIMIT), recentWrapper);

        List<DashboardVO.RecentOrder> recentOrders = recentPage.getRecords()
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

    /**
     * 将分组统计结果转为「状态码 -> 数量」索引。
     * 数据库中不存在某状态时 GROUP BY 不会返回该行，看板需要显式补 0 而非留空，故由 countOf 兜底。
     */
    private Map<Integer, Long> toCountMap(List<StatusCountDTO> rows) {
        Map<Integer, Long> countMap = new HashMap<>();
        if (rows == null) {
            return countMap;
        }
        for (StatusCountDTO row : rows) {
            if (row.getStatus() == null) {
                continue;
            }
            countMap.put(row.getStatus(), row.getCount() == null ? 0L : row.getCount());
        }
        return countMap;
    }

    private long countOf(Map<Integer, Long> countMap, Integer status) {
        return countMap.getOrDefault(status, 0L);
    }

    private String getStatusName(Integer status) {
        return RepairOrderStatus.getName(status);
    }
}
