package com.example.srs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.srs.domain.Blacklist;
import com.example.srs.mapper.BlacklistMapper;
import com.example.srs.service.BlacklistService;
import org.springframework.stereotype.Service;

@Service
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist> implements BlacklistService {

    @Override
    public boolean isUserBlacklisted(String studentId) {
        // 统计该学生状态为 1 (封禁中) 的记录数
        Long count = this.lambdaQuery()
                .eq(Blacklist::getStudentId, studentId)
                .eq(Blacklist::getStatus, 1)
                .count();
        return count > 0;
    }
}
