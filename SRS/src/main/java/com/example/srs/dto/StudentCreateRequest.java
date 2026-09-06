package com.example.srs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentCreateRequest {

    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20位")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "学号只能包含字母、数字、下划线和短横线")
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50位")
    private String realName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度需在6到100位之间")
    private String password;
}
