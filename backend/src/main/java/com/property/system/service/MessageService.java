package com.property.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.entity.Message;

public interface MessageService {
    Page<Message> page(Integer pageNum, Integer pageSize);
    void markRead(Long id);
    Long unreadCount();
}
