package com.example.srs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.srs.domain.Seat;
import com.example.srs.mapper.SeatMapper;
import com.example.srs.service.SeatService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements SeatService {

    @Override
    public List<Seat> getSeatsByRoomId(Long roomId) {
        // 用于前端 Vue 渲染座位网格，查出指定房间的所有座位
        return this.lambdaQuery()
                .eq(Seat::getRoomId, roomId)
                .list();
    }
}
