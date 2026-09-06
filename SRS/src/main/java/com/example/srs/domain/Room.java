package com.example.srs.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

@Data
@TableName("room")
public class Room {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomName;   // 自习室名称
    private Integer floor;     // 楼层
    private Integer totalSeats; // 总座位数
    private Integer status;    // 1-开放, 0-关闭
}
