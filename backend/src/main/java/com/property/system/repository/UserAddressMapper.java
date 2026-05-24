package com.property.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.system.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
