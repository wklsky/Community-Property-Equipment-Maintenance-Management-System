package com.property.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.dto.RepairOrderCreateRequest;
import com.property.system.dto.RepairOrderVO;
import com.property.system.dto.Result;
import com.property.system.entity.RepairOrder;
import com.property.system.security.RequireRole;
import com.property.system.service.ExcelExportService;
import com.property.system.service.RepairOrderService;
import com.property.system.util.RequestContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/repair-orders")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService orderService;
    private final ExcelExportService exportService;

    @RequireRole({"系统管理员", "业主"})
    @PostMapping
    public Result<RepairOrder> create(@Valid @RequestBody RepairOrderCreateRequest request) {
        return Result.success(orderService.create(request));
    }

    @RequireRole("系统管理员")
    @GetMapping
    public Result<Page<RepairOrder>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(orderService.page(pageNum, pageSize, status, priority, orderNo, buildingId, startDate, endDate));
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/my")
    public Result<Page<RepairOrder>> myOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String statuses) {
        List<Integer> statusList = parseStatuses(statuses);
        return Result.success(orderService.myOrders(pageNum, pageSize, statusList));
    }

    @RequireRole({"系统管理员", "维修工"})
    @GetMapping("/assigned")
    public Result<Page<RepairOrder>> assignedOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String statuses) {
        List<Integer> statusList = parseStatuses(statuses);
        return Result.success(orderService.assignedOrders(pageNum, pageSize, statusList));
    }

    private List<Integer> parseStatuses(String statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(statuses.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/{id}")
    public Result<RepairOrderVO> getById(@PathVariable Long id) {
        return Result.success(orderService.getDetailById(id));
    }

    @RequireRole("系统管理员")
    @PostMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long workerId) {
        orderService.assign(id, workerId);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工"})
    @PostMapping("/{id}/accept")
    public Result<Void> accept(@PathVariable Long id) {
        orderService.accept(id);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工"})
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, @RequestParam String processDesc) {
        orderService.complete(id, processDesc);
        return Result.success();
    }

    @RequireRole({"系统管理员", "业主"})
    @PostMapping("/{id}/evaluate")
    public Result<Void> evaluate(@PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment) {
        orderService.evaluate(id, rating, comment);
        return Result.success();
    }

    @RequireRole({"系统管理员", "业主"})
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        orderService.approve(id);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        orderService.reject(id, reason);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @PostMapping("/{id}/transfer")
    public Result<Void> transfer(@PathVariable Long id,
            @RequestParam Long workerId,
            @RequestParam(required = false) String reason) {
        orderService.transfer(id, workerId, reason);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @GetMapping("/{id}/export")
    public void exportSingle(@PathVariable Long id, HttpServletResponse response) throws IOException {
        exportService.exportSingle(id, RequestContext.getTenantId(), response);
    }

    @RequireRole("系统管理员")
    @GetMapping("/export")
    public void exportList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws IOException {
        exportService.exportList(RequestContext.getTenantId(), status, priority,
                orderNo, startDate, endDate, response);
    }

    @RequireRole("系统管理员")
    @GetMapping("/export-statistics")
    public void exportStatistics(HttpServletResponse response) throws IOException {
        exportService.exportStatistics(RequestContext.getTenantId(), response);
    }
}
