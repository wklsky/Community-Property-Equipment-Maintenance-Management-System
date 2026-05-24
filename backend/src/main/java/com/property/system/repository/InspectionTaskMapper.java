package com.property.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.dto.InspectionTaskVO;
import com.property.system.entity.InspectionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InspectionTaskMapper extends BaseMapper<InspectionTask> {

    @Select("<script>" +
            "SELECT t.*, " +
            "b.name as building_name, " +
            "c.name as category_name, " +
            "d.name as device_name, d.location as device_location, " +
            "u.username as assigned_to_name " +
            "FROM inspection_task t " +
            "LEFT JOIN building b ON t.building_id = b.id " +
            "LEFT JOIN device_category c ON t.category_id = c.id " +
            "LEFT JOIN device d ON t.device_id = d.id " +
            "LEFT JOIN sys_user u ON t.assigned_to = u.id " +
            "WHERE t.tenant_id = #{tenantId} " +
            "<if test='status != null'>AND t.status = #{status}</if> " +
            "<if test='assignedTo != null'>AND (t.assigned_to = #{assignedTo} OR t.assigned_to IS NULL)</if> " +
            "ORDER BY t.create_time DESC" +
            "</script>")
    IPage<InspectionTaskVO> selectPageWithDetails(Page<InspectionTaskVO> page,
            @Param("tenantId") Long tenantId,
            @Param("status") Integer status,
            @Param("assignedTo") Long assignedTo);
}
