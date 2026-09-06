package com.example.srs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.srs.domain.Reservation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
}
