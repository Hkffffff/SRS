package com.example.srs.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;
    private Long seatId;
    private LocalDate reservationDate;
    private Integer timeSlot;
    private Integer status;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String seatNumber;

    @TableField(exist = false)
    private String roomName;
}
