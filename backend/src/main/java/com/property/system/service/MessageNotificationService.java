package com.property.system.service;

import com.property.system.entity.Message;
import com.property.system.repository.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageNotificationService {

    private final MessageMapper messageMapper;

    public void send(Long tenantId, Long userId, String type, String content) {
        Message message = new Message();
        message.setTenantId(tenantId);
        message.setUserId(userId);
        message.setType(type);
        message.setContent(content);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
        log.info("消息已发送: tenantId={}, userId={}, type={}, content={}", tenantId, userId, type, content);
    }

    public void notifyOrderCreated(Long tenantId, Long ownerId, String orderNo) {
        send(tenantId, ownerId, "ORDER", "您的报修工单(" + orderNo + ")已提交，请等待处理");
    }

    public void notifyOrderAssigned(Long tenantId, Long workerId, String orderNo) {
        send(tenantId, workerId, "ORDER", "您有新的工单(" + orderNo + ")待处理，请及时查看");
    }

    public void notifyOrderAccepted(Long tenantId, Long ownerId, String orderNo) {
        send(tenantId, ownerId, "ORDER", "您的工单(" + orderNo + ")已有维修工接单，正在处理中");
    }

    public void notifyOrderCompleted(Long tenantId, Long ownerId, String orderNo) {
        send(tenantId, ownerId, "ORDER", "您的工单(" + orderNo + ")已处理完成，请进行评价");
    }

    public void notifyInspectionTaskCreated(Long tenantId, Long workerId, String planName) {
        send(tenantId, workerId, "INSPECTION", "巡检计划'" + planName + "'已生成新的巡检任务，请及时执行");
    }

    public void notifyNoticePublished(Long tenantId, Long userId, String title) {
        send(tenantId, userId, "NOTICE", "社区发布了新公告：" + title);
    }

    public void notifyNewOrderFromInspection(Long tenantId, Long workerId, String orderNo) {
        send(tenantId, workerId, "INSPECTION",
                "巡检异常已自动生成报修工单(" + orderNo + ")，请通知管理员进行派单处理");
    }

    public void notifyOrderApproved(Long tenantId, Long ownerId, String orderNo) {
        send(tenantId, ownerId, "ORDER", "您的工单(" + orderNo + ")已审核通过，等待派单处理");
    }

    public void notifyOrderRejected(Long tenantId, Long ownerId, String orderNo, String reason) {
        String content = "您的工单(" + orderNo + ")已被拒绝";
        if (reason != null && !reason.isEmpty()) {
            content += "，原因：" + reason;
        }
        send(tenantId, ownerId, "ORDER", content);
    }

    public void notifyOrderTransferred(Long tenantId, Long userId, String orderNo, String reason) {
        String content = "工单(" + orderNo + ")已转派给其他维修工";
        if (reason != null && !reason.isEmpty()) {
            content += "，原因：" + reason;
        }
        send(tenantId, userId, "ORDER", content);
    }
}
