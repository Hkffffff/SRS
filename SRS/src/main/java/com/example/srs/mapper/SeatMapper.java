package com.example.srs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.srs.domain.Seat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeatMapper extends BaseMapper<Seat> {
}
