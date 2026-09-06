package com.example.srs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.srs.domain.Blacklist;

public interface BlacklistService extends IService<Blacklist> {

    /**
     * 检查某个学生是否在封禁期内
     * @param studentId 学号
     * @return true-被封禁中，不允许预约；false-正常
     */
    boolean isUserBlacklisted(String studentId);



}