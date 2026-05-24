package com.property.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.entity.Notice;

public interface NoticeService {
    Page<Notice> page(Integer pageNum, Integer pageSize);
    Page<Notice> pageAll(Integer pageNum, Integer pageSize);
    Notice getById(Long id);
    Notice create(Notice notice);
    void update(Notice notice);
    void delete(Long id);
    void publish(Long id);
    void schedulePublish(Long id, String scheduledTime);
    void markRead(Long noticeId);
}
