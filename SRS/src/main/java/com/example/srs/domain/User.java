package com.example.srs.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;

    @JsonIgnore
    private String password;

    private String realName;

    /**
     * 0-学生，1-管理员
     */
    private Integer role;

    private LocalDateTime createTime;
}
