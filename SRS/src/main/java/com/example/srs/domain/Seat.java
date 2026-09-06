package com.example.srs.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

@Data
@TableName("seat")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;       // 所属自习室ID
    private String seatNumber; // 座位编号
    private Integer hasWindow; // 是否靠窗 (0/1)
    private Integer hasPower;  // 是否有插座 (0/1)
    private Integer status;    // 1-正常, 0-损坏
}
