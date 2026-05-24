package com.property.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.dto.DashboardVO;
import com.property.system.dto.RepairOrderVO;
import com.property.system.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {

    @Select("<script>" +
            "SELECT o.*, " +
            "u.username as user_name, u.phone as user_phone, " +
            "d.name as device_name, d.location as device_location, " +
            "w.username as assign_to_name, " +
            "e.rating, e.comment " +
            "FROM repair_order o " +
            "LEFT JOIN sys_user u ON o.user_id = u.id " +
            "LEFT JOIN device d ON o.device_id = d.id " +
            "LEFT JOIN sys_user w ON o.assign_to = w.id " +
            "LEFT JOIN repair_order_evaluation e ON o.id = e.order_id " +
            "WHERE o.tenant_id = #{tenantId} " +
            "<if test='status != null'>AND o.status = #{status}</if> " +
            "<if test='userId != null'>AND o.user_id = #{userId}</if> " +
            "<if test='assignTo != null'>AND o.assign_to = #{assignTo}</if> " +
            "ORDER BY o.create_time DESC" +
            "</script>")
    IPage<RepairOrderVO> selectPageWithDetails(Page<RepairOrderVO> page,
            @Param("tenantId") Long tenantId,
            @Param("status") Integer status,
            @Param("userId") Long userId,
            @Param("assignTo") Long assignTo);

    @Select("SELECT o.*, " +
            "u.username as user_name, u.phone as user_phone, " +
            "d.name as device_name, d.location as device_location, " +
            "w.username as assign_to_name, " +
            "e.rating, e.comment " +
            "FROM repair_order o " +
            "LEFT JOIN sys_user u ON o.user_id = u.id " +
            "LEFT JOIN device d ON o.device_id = d.id " +
            "LEFT JOIN sys_user w ON o.assign_to = w.id " +
            "LEFT JOIN repair_order_evaluation e ON o.id = e.order_id " +
            "WHERE o.id = #{id} AND o.tenant_id = #{tenantId}")
    RepairOrderVO selectDetailById(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Select("<script>" +
            "SELECT DATE(create_time) as date, " +
            "COUNT(*) as count, " +
            "SUM(CASE WHEN status = 5 THEN 1 ELSE 0 END) as completed " +
            "FROM repair_order " +
            "WHERE tenant_id = #{tenantId} AND create_time >= #{startDate} " +
            "<if test='userId != null'>AND user_id = #{userId}</if> " +
            "<if test='workerId != null'>AND assign_to = #{workerId}</if> " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date ASC" +
            "</script>")
    List<DashboardVO.OrderTrendItem> selectOrderTrend(@Param("tenantId") Long tenantId,
                                                      @Param("startDate") String startDate,
                                                      @Param("userId") Long userId,
                                                      @Param("workerId") Long workerId);
}
