package com.property.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.system.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
