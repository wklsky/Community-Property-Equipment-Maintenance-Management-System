package com.property.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.system.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
