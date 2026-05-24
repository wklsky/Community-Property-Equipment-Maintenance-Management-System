package com.property.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.dto.Result;
import com.property.system.entity.Message;
import com.property.system.security.RequireRole;
import com.property.system.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping
    public Result<Page<Message>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(messageService.page(pageNum, pageSize));
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.unreadCount());
    }
}
