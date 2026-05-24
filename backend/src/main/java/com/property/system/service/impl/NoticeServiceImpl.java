package com.property.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.entity.Notice;
import com.property.system.entity.NoticeRead;
import com.property.system.exception.BusinessException;
import com.property.system.repository.NoticeMapper;
import com.property.system.repository.NoticeReadMapper;
import com.property.system.service.NoticeService;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final NoticeReadMapper noticeReadMapper;

    @Override
    public Page<Notice> page(Integer pageNum, Integer pageSize) {
        Long tenantId = RequestContext.getTenantId();

        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getTenantId, tenantId)
                .eq(Notice::getPublishStatus, 1)
                .orderByDesc(Notice::getCreateTime);

        return noticeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Page<Notice> pageAll(Integer pageNum, Integer pageSize) {
        Long tenantId = RequestContext.getTenantId();

        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getTenantId, tenantId)
                .orderByDesc(Notice::getCreateTime);

        return noticeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Notice getById(Long id) {
        return noticeMapper.selectById(id);
    }

    @Override
    public Notice create(Notice notice) {
        notice.setTenantId(RequestContext.getTenantId());
        notice.setPublishStatus(0);
        notice.setCreateTime(LocalDateTime.now());
        noticeMapper.insert(notice);
        return notice;
    }

    @Override
    public void update(Notice notice) {
        noticeMapper.updateById(notice);
    }

    @Override
    public void delete(Long id) {
        noticeMapper.deleteById(id);
    }

    @Override
    public void publish(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        notice.setPublishStatus(1);
        notice.setScheduledTime(null);
        noticeMapper.updateById(notice);
    }

    @Override
    public void schedulePublish(Long id, String scheduledTime) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        try {
            notice.setPublishStatus(2);
            notice.setScheduledTime(LocalDateTime.parse(scheduledTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (DateTimeParseException e) {
            throw new BusinessException("定时发布时间格式错误，正确格式：yyyy-MM-dd HH:mm:ss");
        }
        noticeMapper.updateById(notice);
    }

    @Override
    public void markRead(Long noticeId) {
        Long tenantId = RequestContext.getTenantId();
        Long userId = RequestContext.getUserId();

        Long count = noticeReadMapper.selectCount(new LambdaQueryWrapper<NoticeRead>()
                .eq(NoticeRead::getTenantId, tenantId)
                .eq(NoticeRead::getNoticeId, noticeId)
                .eq(NoticeRead::getUserId, userId));
        if (count > 0) {
            return;
        }

        NoticeRead read = new NoticeRead();
        read.setTenantId(tenantId);
        read.setNoticeId(noticeId);
        read.setUserId(userId);
        read.setReadTime(LocalDateTime.now());
        noticeReadMapper.insert(read);
    }
}
