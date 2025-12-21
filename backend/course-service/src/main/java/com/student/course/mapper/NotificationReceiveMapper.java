package com.student.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.course.entity.NotificationReceive;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知接收记录Mapper
 */
@Mapper
public interface NotificationReceiveMapper extends BaseMapper<NotificationReceive> {
}
