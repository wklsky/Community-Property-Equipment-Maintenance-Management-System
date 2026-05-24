package com.property.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.system.entity.Building;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BuildingMapper extends BaseMapper<Building> {
}
