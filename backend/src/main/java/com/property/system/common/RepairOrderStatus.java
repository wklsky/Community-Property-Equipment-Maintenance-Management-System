package com.property.system.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @Author: kian
 * @Date: 2026-09-01 10:35
 * @LastEditors: kian
 * @LastEditTime: 2026-09-01 10:35
 * @FilePath: backend/src/main/java/com/property/system/common/RepairOrderStatus.java
 * @Description: 工单状态枚举，统一维护状态码与中文名的映射关系
 */

/**
 * 状态流转详见 backend/README.md 的「工单状态流转」章节。
 * 此前状态码的中文名在 OrderStateMachine 与 DashboardController 中各自维护了一份 switch，
 * 且 DashboardController 缺失 TRANSFERRING(7)，导致同一状态在不同接口返回的名称不一致，故收敛到此处。
 */
@Getter
@AllArgsConstructor
public enum RepairOrderStatus {

    PENDING_ACCEPT(0, "待受理"),
    PENDING_ASSIGN(1, "待派单"),
    PENDING_PROCESS(2, "待处理"),
    PROCESSING(3, "处理中"),
    PENDING_EVALUATE(4, "待评价"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消"),
    TRANSFERRING(7, "转单中");

    private final Integer code;

    private final String name;

    public static RepairOrderStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 状态码转中文名。未识别的码值返回「未知(code)」而非抛异常，
     * 避免历史脏数据导致整个列表/看板接口失败
     */
    public static String getName(Integer code) {
        RepairOrderStatus status = of(code);
        return status != null ? status.getName() : "未知(" + code + ")";
    }
}
