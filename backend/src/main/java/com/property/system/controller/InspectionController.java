package com.property.system.controller;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.common.ResultCode;
import com.property.system.dto.Result;
import com.property.system.entity.InspectionPlan;
import com.property.system.entity.InspectionTask;
import com.property.system.entity.InspectionRecord;
import com.property.system.entity.RepairOrder;
import com.property.system.exception.BusinessException;
import com.property.system.repository.InspectionPlanMapper;
import com.property.system.repository.InspectionTaskMapper;
import com.property.system.repository.InspectionRecordMapper;
import com.property.system.repository.RepairOrderMapper;
import com.property.system.security.RequireRole;
import com.property.system.service.MessageNotificationService;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionPlanMapper planMapper;
    private final InspectionTaskMapper taskMapper;
    private final InspectionRecordMapper recordMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final MessageNotificationService notificationService;

    @RequireRole({"系统管理员", "维修工"})
    @GetMapping("/plans")
    public Result<Page<InspectionPlan>> plans(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String name) {
        return Result.success(planMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InspectionPlan>()
                        .like(name != null && !name.isEmpty(), InspectionPlan::getName, name)
                        .eq(status != null, InspectionPlan::getStatus, status)
                        .orderByDesc(InspectionPlan::getCreateTime)));
    }

    @RequireRole({"系统管理员", "维修工"})
    @GetMapping("/plans/{id}")
    public Result<InspectionPlan> getPlan(@PathVariable Long id) {
        InspectionPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.INSPECTION_PLAN_NOT_FOUND);
        }
        return Result.success(plan);
    }

    @RequireRole("系统管理员")
    @PostMapping("/plans")
    public Result<InspectionPlan> createPlan(@RequestBody InspectionPlan plan) {
        if (plan.getName() == null || plan.getName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "计划名称不能为空");
        }
        if (plan.getCycle() == null || plan.getCycle() <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "巡检周期必须大于0");
        }

        plan.setTenantId(RequestContext.getTenantId());
        plan.setStatus(0);
        plan.setCreateTime(LocalDateTime.now());
        planMapper.insert(plan);

        log.info("创建巡检计划: id={}, name={}", plan.getId(), plan.getName());
        return Result.success(plan);
    }

    @RequireRole("系统管理员")
    @PutMapping("/plans/{id}")
    public Result<Void> updatePlan(@PathVariable Long id, @RequestBody InspectionPlan plan) {
        InspectionPlan existingPlan = planMapper.selectById(id);
        if (existingPlan == null) {
            throw new BusinessException(ResultCode.INSPECTION_PLAN_NOT_FOUND);
        }

        if (existingPlan.getStatus() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已发布的计划不能修改");
        }

        plan.setId(id);
        plan.setTenantId(null);
        plan.setStatus(null);
        planMapper.updateById(plan);

        log.info("更新巡检计划: id={}", id);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @DeleteMapping("/plans/{id}")
    @Transactional
    public Result<Void> deletePlan(@PathVariable Long id) {
        InspectionPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.INSPECTION_PLAN_NOT_FOUND);
        }

        Long pendingTaskCount = taskMapper.selectCount(new LambdaQueryWrapper<InspectionTask>()
                .eq(InspectionTask::getPlanId, id)
                .ne(InspectionTask::getStatus, 2));

        if (pendingTaskCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该计划下存在未完成的任务，无法删除");
        }

        planMapper.deleteById(id);
        log.info("删除巡检计划: id={}", id);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @PostMapping("/plans/{id}/publish")
    @Transactional
    public Result<Void> publishPlan(@PathVariable Long id) {
        Long tenantId = RequestContext.getTenantId();

        InspectionPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.INSPECTION_PLAN_NOT_FOUND);
        }

        if (plan.getStatus() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "计划已发布，请勿重复操作");
        }

        if (plan.getCycle() == null || plan.getCycle() <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先设置有效的巡检周期");
        }

        plan.setStatus(1);
        LocalDate nextTime = LocalDate.now().plusDays(plan.getCycle());
        plan.setNextTime(nextTime);
        planMapper.updateById(plan);

        InspectionTask task = new InspectionTask();
        task.setTenantId(tenantId);
        task.setPlanId(plan.getId());
        task.setPlanName(plan.getName());
        task.setBuildingId(plan.getBuildingId());
        task.setCategoryId(plan.getCategoryId());
        task.setDeviceId(plan.getDeviceId());
        task.setTaskDate(LocalDate.now());
        task.setStatus(0);
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);

        log.info("发布巡检计划: planId={}, taskId={}, nextTime={}", plan.getId(), task.getId(), nextTime);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @PostMapping("/plans/{id}/pause")
    public Result<Void> pausePlan(@PathVariable Long id) {
        InspectionPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.INSPECTION_PLAN_NOT_FOUND);
        }

        if (plan.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只有已发布的计划才能暂停");
        }

        plan.setStatus(2);
        planMapper.updateById(plan);

        log.info("暂停巡检计划: id={}", id);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @PostMapping("/plans/{id}/resume")
    public Result<Void> resumePlan(@PathVariable Long id) {
        InspectionPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.INSPECTION_PLAN_NOT_FOUND);
        }

        if (plan.getStatus() != 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只有暂停的计划才能恢复");
        }

        plan.setStatus(1);
        plan.setNextTime(LocalDate.now().plusDays(plan.getCycle()));
        planMapper.updateById(plan);

        log.info("恢复巡检计划: id={}", id);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工"})
    @GetMapping("/tasks")
    public Result<Page<InspectionTask>> tasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long planId) {
        Long tenantId = RequestContext.getTenantId();
        return Result.success(taskMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InspectionTask>()
                        .eq(InspectionTask::getTenantId, tenantId)
                        .eq(planId != null, InspectionTask::getPlanId, planId)
                        .eq(status != null, InspectionTask::getStatus, status)
                        .orderByDesc(InspectionTask::getCreateTime)));
    }

    @RequireRole({"系统管理员", "维修工"})
    @GetMapping("/tasks/my")
    public Result<Page<InspectionTask>> myTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();
        return Result.success(taskMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InspectionTask>()
                        .eq(InspectionTask::getTenantId, tenantId)
                        .and(w -> w.eq(InspectionTask::getAssignedTo, userId)
                                .or().isNull(InspectionTask::getAssignedTo))
                        .eq(status != null, InspectionTask::getStatus, status)
                        .orderByDesc(InspectionTask::getCreateTime)));
    }

    @RequireRole({"系统管理员", "维修工"})
    @GetMapping("/tasks/{id}")
    public Result<InspectionTask> getTask(@PathVariable Long id) {
        InspectionTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.INSPECTION_TASK_NOT_FOUND);
        }
        return Result.success(task);
    }

    @RequireRole({"系统管理员", "维修工"})
    @PostMapping("/tasks/{id}/accept")
    public Result<Void> acceptTask(@PathVariable Long id) {
        Long userId = RequestContext.getUserId();

        InspectionTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.INSPECTION_TASK_NOT_FOUND);
        }

        if (task.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该任务已被接单或已完成");
        }

        if (task.getAssignedTo() != null && !task.getAssignedTo().equals(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该任务已被其他人接单");
        }

        task.setAssignedTo(userId);
        task.setStatus(1);
        taskMapper.updateById(task);

        log.info("接单巡检任务: taskId={}, userId={}", id, userId);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工"})
    @PostMapping("/tasks/{id}/complete")
    @Transactional
    public Result<Void> completeTask(@PathVariable Long id, @RequestBody InspectionRecord record) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        InspectionTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.INSPECTION_TASK_NOT_FOUND);
        }

        if (task.getStatus() == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先接单再提交巡检记录");
        }
        if (task.getStatus() == 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该任务已完成");
        }

        if (task.getAssignedTo() == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该任务未被接单，无法完成");
        }
        if (!task.getAssignedTo().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该任务的负责人，无法提交完成");
        }

        if (record.getResult() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请填写巡检结果");
        }

        task.setStatus(2);
        taskMapper.updateById(task);

        record.setTenantId(tenantId);
        record.setTaskId(id);
        record.setDeviceId(task.getDeviceId());
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);

        RepairOrder generatedOrder = null;
        if (record.getResult() == 0) {
            generatedOrder = new RepairOrder();
            generatedOrder.setTenantId(tenantId);
            generatedOrder.setOrderNo("ORD" + IdUtil.getSnowflakeNextIdStr());
            generatedOrder.setUserId(userId);
            generatedOrder.setDeviceId(task.getDeviceId());
            generatedOrder.setFaultDesc("巡检异常-" + task.getPlanName()
                    + (record.getRemark() != null ? "：" + record.getRemark() : ""));
            generatedOrder.setPriority(2);
            generatedOrder.setStatus(0);
            generatedOrder.setCreateTime(LocalDateTime.now());
            generatedOrder.setAddress(task.getBuildingId() != null
                    ? "楼栋ID:" + task.getBuildingId() : null);
            repairOrderMapper.insert(generatedOrder);

            notificationService.notifyNewOrderFromInspection(tenantId, userId,
                    generatedOrder.getOrderNo());

            log.info("巡检异常自动生成工单: taskId={}, orderId={}, orderNo={}",
                    id, generatedOrder.getId(), generatedOrder.getOrderNo());
        }

        log.info("完成巡检任务: taskId={}, recordId={}, result={}, autoOrder={}",
                id, record.getId(), record.getResult(),
                generatedOrder != null ? generatedOrder.getOrderNo() : "无");

        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工"})
    @GetMapping("/tasks/{id}/records")
    public Result<Page<InspectionRecord>> taskRecords(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(recordMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InspectionRecord>()
                        .eq(InspectionRecord::getTaskId, id)
                        .orderByDesc(InspectionRecord::getCreateTime)));
    }
}
