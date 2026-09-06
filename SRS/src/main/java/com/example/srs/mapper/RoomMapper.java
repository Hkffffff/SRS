package com.example.srs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.srs.domain.Room;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {
}