package com.example.srs.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("blacklist")
public class Blacklist {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;     // 学号
    private String reason;        // 违规原因
    private LocalDateTime violationDate;
    private Integer status;       // 1-封禁中, 0-已解除
}