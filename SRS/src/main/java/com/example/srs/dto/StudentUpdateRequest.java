package com.example.srs.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentUpdateRequest {

    @Size(min = 1, max = 50, message = "姓名长度需在1到50位之间")
    private String realName;

    @Size(min = 6, max = 100, message = "密码长度需在6到100位之间")
    private String password;
}
