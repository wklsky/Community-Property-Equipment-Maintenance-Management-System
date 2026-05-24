package com.property.system.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.dto.RepairOrderCreateRequest;
import com.property.system.dto.RepairOrderVO;
import com.property.system.entity.RepairOrder;
import com.property.system.entity.RepairOrderEvaluation;
import com.property.system.entity.RepairOrderLog;
import com.property.system.exception.BusinessException;
import com.property.system.repository.RepairOrderEvaluationMapper;
import com.property.system.repository.RepairOrderLogMapper;
import com.property.system.repository.RepairOrderMapper;
import com.property.system.service.MessageNotificationService;
import com.property.system.service.OrderStateMachine;
import com.property.system.service.RepairOrderService;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl implements RepairOrderService {

    private final RepairOrderMapper orderMapper;
    private final RepairOrderLogMapper logMapper;
    private final RepairOrderEvaluationMapper evaluationMapper;
    private final OrderStateMachine stateMachine;
    private final MessageNotificationService notificationService;

    @Override
    @Transactional
    public RepairOrder create(RepairOrderCreateRequest request) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        RepairOrder order = new RepairOrder();
        order.setTenantId(tenantId);
        order.setOrderNo("ORD" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setDeviceId(request.getDeviceId());
        order.setAddress(request.getAddress());
        order.setFaultDesc(request.getFaultDesc());
        order.setPriority(request.getPriority() != null ? request.getPriority() : 1);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        if (request.getAppointTime() != null && !request.getAppointTime().isEmpty()) {
            order.setAppointTime(LocalDateTime.parse(request.getAppointTime(),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        orderMapper.insert(order);

        saveLog(order.getId(), tenantId, userId, "创建工单", "业主提交");

        return order;
    }

    @Override
    public Page<RepairOrder> page(Integer pageNum, Integer pageSize, Integer status,
            Integer priority, String orderNo, Long buildingId, String startDate, String endDate) {
        Long tenantId = RequestContext.getTenantId();

        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getTenantId, tenantId)
                .eq(status != null, RepairOrder::getStatus, status)
                .eq(priority != null, RepairOrder::getPriority, priority)
                .like(orderNo != null && !orderNo.isEmpty(), RepairOrder::getOrderNo, orderNo)
                .like(buildingId != null, RepairOrder::getAddress, "楼栋ID:" + buildingId)
                .orderByDesc(RepairOrder::getCreateTime);

        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(RepairOrder::getCreateTime,
                    LocalDateTime.of(LocalDate.parse(startDate), LocalTime.MIN));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(RepairOrder::getCreateTime,
                    LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX));
        }

        return orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Page<RepairOrder> myOrders(Integer pageNum, Integer pageSize, List<Integer> statuses) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getTenantId, tenantId)
                .eq(RepairOrder::getUserId, userId)
                .in(statuses != null && !statuses.isEmpty(), RepairOrder::getStatus, statuses)
                .orderByDesc(RepairOrder::getCreateTime);

        return orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Page<RepairOrder> assignedOrders(Integer pageNum, Integer pageSize, List<Integer> statuses) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getTenantId, tenantId)
                .eq(RepairOrder::getAssignTo, userId)
                .in(statuses != null && !statuses.isEmpty(), RepairOrder::getStatus, statuses)
                .orderByDesc(RepairOrder::getCreateTime);

        return orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public RepairOrder getById(Long id) {
        Long tenantId = RequestContext.getTenantId();
        RepairOrder order = orderMapper.selectById(id);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new BusinessException("工单不存在");
        }
        return order;
    }

    @Override
    public RepairOrderVO getDetailById(Long id) {
        Long tenantId = RequestContext.getTenantId();
        RepairOrderVO vo = orderMapper.selectDetailById(id, tenantId);
        if (vo == null) {
            throw new BusinessException("工单不存在");
        }
        return vo;
    }

    @Override
    @Transactional
    public void assign(Long orderId, Long workerId) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new BusinessException("工单不存在");
        }

        stateMachine.validateTransition(order.getStatus(), 2, "派单");
        order.setAssignTo(workerId);
        order.setStatus(2);
        orderMapper.updateById(order);

        saveLog(orderId, tenantId, userId, "派单", "分配维修工");
        notificationService.notifyOrderAssigned(tenantId, workerId, order.getOrderNo());
    }

    @Override
    @Transactional
    public void accept(Long orderId) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new BusinessException("工单不存在");
        }

        if (!order.getAssignTo().equals(userId)) {
            throw new BusinessException("您不是该工单的指派维修工");
        }
        stateMachine.validateTransition(order.getStatus(), 3, "开始处理");
        order.setStatus(3);
        orderMapper.updateById(order);

        saveLog(orderId, tenantId, userId, "开始处理", "已上门");
        notificationService.notifyOrderAccepted(tenantId, order.getUserId(), order.getOrderNo());
    }

    @Override
    @Transactional
    public void complete(Long orderId, String processDesc) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new BusinessException("工单不存在");
        }

        if (!order.getAssignTo().equals(userId)) {
            throw new BusinessException("您不是该工单的指派维修工");
        }
        stateMachine.validateTransition(order.getStatus(), 4, "完成处理");
        order.setStatus(4);
        order.setProcessDesc(processDesc);
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateById(order);

        saveLog(orderId, tenantId, userId, "完成", "已修复");
        notificationService.notifyOrderCompleted(tenantId, order.getUserId(), order.getOrderNo());
    }

    @Override
    @Transactional
    public void evaluate(Long orderId, Integer rating, String comment) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new BusinessException("工单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("只有工单创建者才能评价");
        }
        stateMachine.validateTransition(order.getStatus(), 5, "评价");
        order.setStatus(5);
        orderMapper.updateById(order);

        RepairOrderEvaluation evaluation = new RepairOrderEvaluation();
        evaluation.setTenantId(tenantId);
        evaluation.setOrderId(orderId);
        evaluation.setRating(rating);
        evaluation.setComment(comment);
        evaluation.setCreateTime(LocalDateTime.now());
        evaluationMapper.insert(evaluation);

        saveLog(orderId, tenantId, userId, "评价", "评分: " + rating);
    }

    @Override
    @Transactional
    public void cancel(Long orderId) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();
        String role = RequestContext.getRole();

        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new BusinessException("工单不存在");
        }

        boolean isAdmin = "系统管理员".equals(role);
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new BusinessException("只有工单创建者或管理员才能取消");
        }
        stateMachine.validateTransition(order.getStatus(), 6, "取消工单");
        order.setStatus(6);
        orderMapper.updateById(order);

        String remark = isAdmin ? "管理员取消" : "用户取消";
        saveLog(orderId, tenantId, userId, "取消", remark);
    }

    @Override
    @Transactional
    public void approve(Long orderId) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new BusinessException("工单不存在");
        }

        stateMachine.validateTransition(order.getStatus(), 1, "审核通过");
        order.setStatus(1);
        orderMapper.updateById(order);

        saveLog(orderId, tenantId, userId, "审核通过", "管理员审核通过");
        notificationService.notifyOrderApproved(tenantId, order.getUserId(), order.getOrderNo());
    }

    @Override
    @Transactional
    public void reject(Long orderId, String reason) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new BusinessException("工单不存在");
        }

        stateMachine.validateTransition(order.getStatus(), 6, "拒绝工单");
        order.setStatus(6);
        order.setTransferReason(reason);
        orderMapper.updateById(order);

        saveLog(orderId, tenantId, userId, "拒绝", reason);
        notificationService.notifyOrderRejected(tenantId, order.getUserId(), order.getOrderNo(), reason);
    }

    @Override
    @Transactional
    public void transfer(Long orderId, Long newWorkerId, String reason) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new BusinessException("工单不存在");
        }

        Integer currentStatus = order.getStatus();

        int targetStatus = 2;
        stateMachine.validateTransition(currentStatus, targetStatus, "转派");

        order.setAssignTo(newWorkerId);
        order.setStatus(targetStatus);
        order.setTransferReason(reason);
        orderMapper.updateById(order);

        saveLog(orderId, tenantId, userId, "转派", "转派给维修工ID:" + newWorkerId + "，原因:" + reason);
        notificationService.notifyOrderTransferred(tenantId, newWorkerId, order.getOrderNo(), reason);
        if (order.getUserId() != null) {
            notificationService.notifyOrderTransferred(tenantId, order.getUserId(), order.getOrderNo(), reason);
        }
    }

    private void saveLog(Long orderId, Long tenantId, Long operatorId, String action, String remark) {
        RepairOrderLog log = new RepairOrderLog();
        log.setTenantId(tenantId);
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }
}
