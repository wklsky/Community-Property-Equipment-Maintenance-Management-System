package com.property.system.service;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.property.system.dto.RepairOrderVO;
import com.property.system.entity.RepairOrder;
import com.property.system.repository.RepairOrderMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final RepairOrderMapper orderMapper;

    private static final Map<Integer, String> STATUS_MAP = Map.of(
            0, "待受理", 1, "待派单", 2, "待处理", 3, "处理中",
            4, "待评价", 5, "已完成", 6, "已取消", 7, "转单中"
    );

    public void exportSingle(Long orderId, Long tenantId, HttpServletResponse response) throws IOException {
        RepairOrderVO vo = orderMapper.selectDetailById(orderId, tenantId);
        if (vo == null) return;

        ExcelWriter writer = ExcelUtil.getWriter(true);
        setResponseHeader(response, "工单_" + vo.getOrderNo() + ".xlsx");

        writer.addHeaderAlias("orderNo", "工单号");
        writer.addHeaderAlias("statusName", "状态");
        writer.addHeaderAlias("address", "报修地址");
        writer.addHeaderAlias("faultDesc", "故障描述");
        writer.addHeaderAlias("priorityName", "优先级");
        writer.addHeaderAlias("userName", "报修人");
        writer.addHeaderAlias("userPhone", "联系电话");
        writer.addHeaderAlias("deviceName", "关联设备");
        writer.addHeaderAlias("assignToName", "维修工");
        writer.addHeaderAlias("appointTime", "预约时间");
        writer.addHeaderAlias("processDesc", "处理说明");
        writer.addHeaderAlias("transferReason", "转派/拒绝原因");
        writer.addHeaderAlias("rating", "评分");
        writer.addHeaderAlias("comment", "评价内容");
        writer.addHeaderAlias("createTime", "创建时间");
        writer.addHeaderAlias("finishTime", "完成时间");

        fillVoData(vo);
        writer.write(List.of(vo), true);
        autoSizeColumns(writer);
        writer.flush(response.getOutputStream());
        writer.close();
    }

    public void exportList(Long tenantId, Integer status, Integer priority,
            String orderNo, String startDate, String endDate, HttpServletResponse response) throws IOException {
        List<RepairOrder> orders = queryOrders(tenantId, status, priority, orderNo, startDate, endDate);

        ExcelWriter writer = ExcelUtil.getWriter(true);
        setResponseHeader(response, "工单列表_" + today() + ".xlsx");

        writer.addHeaderAlias("orderNo", "工单号");
        writer.addHeaderAlias("address", "报修地址");
        writer.addHeaderAlias("faultDesc", "故障描述");
        writer.addHeaderAlias("status", "状态");
        writer.addHeaderAlias("priority", "优先级");
        writer.addHeaderAlias("createTime", "创建时间");
        writer.addHeaderAlias("appointTime", "预约时间");
        writer.addHeaderAlias("finishTime", "完成时间");
        writer.addHeaderAlias("processDesc", "处理说明");
        writer.addHeaderAlias("transferReason", "转派/拒绝原因");

        List<Map<String, Object>> rows = orders.stream().map(o -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("orderNo", o.getOrderNo());
            row.put("address", o.getAddress());
            row.put("faultDesc", o.getFaultDesc());
            row.put("status", STATUS_MAP.getOrDefault(o.getStatus(), "未知"));
            row.put("priority", o.getPriority() == 1 ? "紧急" : "普通");
            row.put("createTime", fmt(o.getCreateTime()));
            row.put("appointTime", fmt(o.getAppointTime()));
            row.put("finishTime", fmt(o.getFinishTime()));
            row.put("processDesc", o.getProcessDesc());
            row.put("transferReason", o.getTransferReason());
            return row;
        }).collect(Collectors.toList());

        writer.write(rows, true);
        autoSizeColumns(writer);
        writer.flush(response.getOutputStream());
        writer.close();
    }

    public void exportStatistics(Long tenantId, HttpServletResponse response) throws IOException {
        List<RepairOrder> orders = queryOrders(tenantId, null, null, null, null, null);

        ExcelWriter writer = ExcelUtil.getWriter(true);
        setResponseHeader(response, "工单统计报表_" + today() + ".xlsx");

        writer.setSheet("状态分布");
        Map<Integer, Long> statusCount = orders.stream()
                .collect(Collectors.groupingBy(RepairOrder::getStatus, Collectors.counting()));
        List<Map<String, Object>> statusRows = new ArrayList<>();
        long total = orders.size();
        for (int s = 0; s <= 7; s++) {
            long count = statusCount.getOrDefault(s, 0L);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("status", STATUS_MAP.getOrDefault(s, "未知"));
            row.put("count", count);
            row.put("percent", total > 0 ? String.format("%.1f%%", count * 100.0 / total) : "0%");
            statusRows.add(row);
        }
        writer.addHeaderAlias("status", "状态");
        writer.addHeaderAlias("count", "数量");
        writer.addHeaderAlias("percent", "占比");
        writer.write(statusRows, true);
        autoSizeColumns(writer);

        writer.setSheet("优先级分布");
        Map<Integer, Long> priorityCount = orders.stream()
                .collect(Collectors.groupingBy(RepairOrder::getPriority, Collectors.counting()));
        List<Map<String, Object>> priorityRows = new ArrayList<>();
        priorityRows.add(mapRow("普通", priorityCount.getOrDefault(0, 0L)));
        priorityRows.add(mapRow("紧急", priorityCount.getOrDefault(1, 0L)));
        writer.addHeaderAlias("label", "优先级");
        writer.addHeaderAlias("value", "数量");
        writer.write(priorityRows, true);
        autoSizeColumns(writer);

        writer.setSheet("维修工工作量");
        Map<Long, Long> workerCount = orders.stream()
                .filter(o -> o.getAssignTo() != null)
                .collect(Collectors.groupingBy(RepairOrder::getAssignTo, Collectors.counting()));
        List<Map<String, Object>> workerRows = workerCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("workerId", e.getKey());
                    row.put("orderCount", e.getValue());
                    return row;
                }).collect(Collectors.toList());
        writer.addHeaderAlias("workerId", "维修工ID");
        writer.addHeaderAlias("orderCount", "工单数量");
        writer.write(workerRows, true);
        autoSizeColumns(writer);

        writer.setSheet("月度趋势");
        Map<String, Long> monthCount = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime() != null ? o.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM")) : "未知",
                        Collectors.counting()));
        List<Map<String, Object>> monthRows = monthCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("month", e.getKey());
                    row.put("count", e.getValue());
                    return row;
                }).collect(Collectors.toList());
        writer.addHeaderAlias("month", "月份");
        writer.addHeaderAlias("count", "工单数量");
        writer.write(monthRows, true);
        autoSizeColumns(writer);

        writer.flush(response.getOutputStream());
        writer.close();
    }

    private List<RepairOrder> queryOrders(Long tenantId, Integer status, Integer priority,
            String orderNo, String startDate, String endDate) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getTenantId, tenantId)
                .eq(status != null, RepairOrder::getStatus, status)
                .eq(priority != null, RepairOrder::getPriority, priority)
                .like(orderNo != null && !orderNo.isEmpty(), RepairOrder::getOrderNo, orderNo)
                .orderByDesc(RepairOrder::getCreateTime);

        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(RepairOrder::getCreateTime,
                    LocalDateTime.of(java.time.LocalDate.parse(startDate), java.time.LocalTime.MIN));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(RepairOrder::getCreateTime,
                    LocalDateTime.of(java.time.LocalDate.parse(endDate), java.time.LocalTime.MAX));
        }
        return orderMapper.selectList(wrapper);
    }

    private void fillVoData(RepairOrderVO vo) {
        vo.setStatusName(STATUS_MAP.getOrDefault(vo.getStatus(), "未知"));
        vo.setPriorityName(vo.getPriority() == 1 ? "紧急" : "普通");
    }

    private Map<String, Object> mapRow(String label, Long value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("label", label);
        row.put("value", value);
        return row;
    }

    private void autoSizeColumns(ExcelWriter writer) {
        Sheet sheet = writer.getSheet();
        if (sheet == null) return;
        int colCount = sheet.getRow(0) != null ? sheet.getRow(0).getLastCellNum() : 0;
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
            int width = Math.min(sheet.getColumnWidth(i) + 2048, 30 * 256);
            sheet.setColumnWidth(i, width);
        }
    }

    private void setResponseHeader(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
    }

    private String fmt(LocalDateTime dt) {
        return dt != null ? dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
    }

    private String today() {
        return java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
