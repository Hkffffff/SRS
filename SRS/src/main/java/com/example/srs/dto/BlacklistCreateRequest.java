package com.example.srs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlacklistCreateRequest {

    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotBlank(message = "封禁原因不能为空")
    private String reason;
}
