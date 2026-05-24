package com.property.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.entity.Message;
import com.property.system.exception.BusinessException;
import com.property.system.repository.MessageMapper;
import com.property.system.service.MessageService;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public Page<Message> page(Integer pageNum, Integer pageSize) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getTenantId, tenantId)
                .eq(Message::getUserId, userId)
                .orderByDesc(Message::getCreateTime);

        return messageMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void markRead(Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null || !message.getUserId().equals(RequestContext.getUserId())) {
            throw new BusinessException("消息不存在");
        }
        message.setIsRead(1);
        messageMapper.updateById(message);
    }

    @Override
    public Long unreadCount() {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        return messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getTenantId, tenantId)
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
    }
}
