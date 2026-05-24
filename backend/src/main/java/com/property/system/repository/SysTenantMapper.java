package com.property.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.system.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}
