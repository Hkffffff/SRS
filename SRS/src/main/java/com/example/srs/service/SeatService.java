package com.example.srs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.srs.domain.Seat;

import java.util.List;

public interface SeatService extends IService<Seat> {

    List<Seat> getSeatsByRoomId(Long roomId);
}