package com.property.system.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardVO {

    private OrderStats orderStats;

    private DeviceStats deviceStats;

    private InspectionStats inspectionStats;

    private List<OrderTrendItem> orderTrend;

    private List<RecentOrder> recentOrders;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderStats {

        private Long pendingAccept;

        private Long pendingAssign;

        private Long pending;

        private Long processing;

        private Long pendingEvaluate;

        private Long completed;

        private Long cancelled;

        private Long total;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeviceStats {

        private Long normal;

        private Long faulty;

        private Long repairing;

        private Long disabled;

        private Long total;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InspectionStats {

        private Long pending;

        private Long processing;

        private Long completedToday;

        private Long total;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderTrendItem {

        private String date;

        private Long count;

        private Long completed;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentOrder {
        private Long id;
        private String orderNo;
        private String faultDesc;
        private String address;
        private Integer status;
        private String statusName;
        private String createTime;
    }
}
