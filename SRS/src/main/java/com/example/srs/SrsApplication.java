package com.example.srs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.srs.mapper") // 必须精确指向你存放 Mapper 接口的包路径
public class SrsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SrsApplication.class, args);
    }
}
