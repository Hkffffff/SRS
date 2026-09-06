package com.example.srs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.srs.domain.User;
import com.example.srs.mapper.UserMapper;
import com.example.srs.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(String studentId, String password) {
        // 使用 MyBatis-Plus 的 LambdaQueryWrapper，避免手写 SQL，防止拼写错误
        return this.lambdaQuery()
                .eq(User::getStudentId, studentId)
                .eq(User::getPassword, password)
                .one(); // 查询单条记录，查不到返回 null
    }
}