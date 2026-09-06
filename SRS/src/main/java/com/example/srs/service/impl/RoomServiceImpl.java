package com.example.srs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.srs.domain.Room;
import com.example.srs.mapper.RoomMapper;
import com.example.srs.service.RoomService;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {
    // 基础管理后台的增删改查，父类 ServiceImpl 已经全部提供，无需写任何代码
}