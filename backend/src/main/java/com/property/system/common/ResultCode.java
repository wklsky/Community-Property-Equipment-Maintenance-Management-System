package com.property.system.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "数据冲突"),
    VALIDATION_ERROR(422, "参数校验失败"),

    INTERNAL_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PASSWORD_ERROR(1003, "密码错误"),
    TOKEN_INVALID(1004, "Token无效或已过期"),
    TOKEN_EXPIRED(1005, "Token已过期"),

    NO_PERMISSION(2001, "无操作权限"),
    ROLE_NOT_FOUND(2002, "角色不存在"),
    PERMISSION_DENIED(2003, "权限不足"),

    TENANT_NOT_FOUND(3001, "租户不存在"),
    TENANT_DISABLED(3002, "租户已被禁用"),
    TENANT_MISMATCH(3003, "租户数据不匹配"),

    ORDER_NOT_FOUND(4001, "工单不存在"),
    ORDER_STATUS_ERROR(4002, "工单状态不允许此操作"),
    ORDER_ALREADY_ASSIGNED(4003, "工单已被分配"),

    DEVICE_NOT_FOUND(5001, "设备不存在"),
    DEVICE_DISABLED(5002, "设备已停用"),

    INSPECTION_PLAN_NOT_FOUND(6001, "巡检计划不存在"),
    INSPECTION_TASK_NOT_FOUND(6002, "巡检任务不存在"),

    NOTICE_NOT_FOUND(7001, "公告不存在");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
