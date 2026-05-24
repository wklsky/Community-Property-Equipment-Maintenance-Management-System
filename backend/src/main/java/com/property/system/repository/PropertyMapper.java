package com.property.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.system.dto.MyPropertyVO;
import com.property.system.entity.Property;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PropertyMapper extends BaseMapper<Property> {

    @Select("SELECT p.id, p.is_default AS isDefault, " +
            "c.id AS communityId, c.name AS communityName, " +
            "b.id AS buildingId, b.name AS buildingName, " +
            "r.id AS roomId, r.room_no AS roomNo, " +
            "CONCAT(c.name, ' ', b.name, ' ', r.room_no) AS fullAddress " +
            "FROM property p " +
            "JOIN room r ON p.room_id = r.id " +
            "JOIN building b ON r.building_id = b.id " +
            "JOIN community c ON b.community_id = c.id " +
            "WHERE p.tenant_id = #{tenantId} AND p.owner_id = #{userId} " +
            "ORDER BY p.is_default DESC, p.id ASC")
    List<MyPropertyVO> listMyProperties(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
