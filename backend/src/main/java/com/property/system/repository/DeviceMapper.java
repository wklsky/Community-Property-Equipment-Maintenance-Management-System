package com.property.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.system.dto.DeviceVO;
import com.property.system.dto.StatusCountDTO;
import com.property.system.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    @Select("<script>" +
            "SELECT d.*, " +
            "b.name as building_name, " +
            "c.name as category_name " +
            "FROM device d " +
            "LEFT JOIN building b ON d.building_id = b.id " +
            "LEFT JOIN device_category c ON d.category_id = c.id " +
            "WHERE d.tenant_id = #{tenantId} " +
            "<if test='categoryId != null'>AND d.category_id = #{categoryId}</if> " +
            "<if test='buildingId != null'>AND d.building_id = #{buildingId}</if> " +
            "<if test='status != null'>AND d.status = #{status}</if> " +
            "ORDER BY d.create_time DESC" +
            "</script>")
    IPage<DeviceVO> selectPageWithDetails(Page<DeviceVO> page,
            @Param("tenantId") Long tenantId,
            @Param("categoryId") Long categoryId,
            @Param("buildingId") Long buildingId,
            @Param("status") Integer status);

    @Select("SELECT d.*, " +
            "b.name as building_name, " +
            "c.name as category_name " +
            "FROM device d " +
            "LEFT JOIN building b ON d.building_id = b.id " +
            "LEFT JOIN device_category c ON d.category_id = c.id " +
            "WHERE d.id = #{id} AND d.tenant_id = #{tenantId}")
    DeviceVO selectDetailById(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * 一次性统计各状态设备数量，替代看板按状态逐个 selectCount 的 5 次查询
     */
    @Select("SELECT status as status, COUNT(*) as count " +
            "FROM device " +
            "WHERE tenant_id = #{tenantId} " +
            "GROUP BY status")
    List<StatusCountDTO> countGroupByStatus(@Param("tenantId") Long tenantId);
}
