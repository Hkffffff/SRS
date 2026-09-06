package com.example.srs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.srs.domain.User;

public interface UserService extends IService<User> {

    User login(String studentId, String password);
}
