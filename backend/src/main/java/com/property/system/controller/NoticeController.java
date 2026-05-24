package com.property.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.dto.Result;
import com.property.system.entity.Notice;
import com.property.system.security.RequireRole;
import com.property.system.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping
    public Result<Page<Notice>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(noticeService.page(pageNum, pageSize));
    }

    @RequireRole("系统管理员")
    @GetMapping("/all")
    public Result<Page<Notice>> pageAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(noticeService.pageAll(pageNum, pageSize));
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable Long id) {
        return Result.success(noticeService.getById(id));
    }

    @RequireRole("系统管理员")
    @PostMapping
    public Result<Notice> create(@RequestBody Notice notice) {
        return Result.success(noticeService.create(notice));
    }

    @RequireRole("系统管理员")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        noticeService.update(notice);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        noticeService.publish(id);
        return Result.success();
    }

    @RequireRole("系统管理员")
    @PostMapping("/{id}/schedule")
    public Result<Void> schedulePublish(@PathVariable Long id, @RequestParam String scheduledTime) {
        noticeService.schedulePublish(id, scheduledTime);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        noticeService.markRead(id);
        return Result.success();
    }
}
