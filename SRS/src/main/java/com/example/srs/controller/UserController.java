package com.example.srs.controller;

import com.example.srs.auth.AuthConstants;
import com.example.srs.auth.AuthHelper;
import com.example.srs.auth.LoginUser;
import com.example.srs.common.Result;
import com.example.srs.domain.User;
import com.example.srs.dto.LoginRequest;
import com.example.srs.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<User> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        User user = userService.login(request.getStudentId().trim(), request.getPassword());
        if (user != null) {
            session.setAttribute(AuthConstants.LOGIN_USER,
                    new LoginUser(user.getId(), user.getStudentId(), user.getRealName(), user.getRole()));
            return Result.success("登录成功", user);
        }
        return Result.error("学号或密码错误");
    }

    @GetMapping("/me")
    public Result<LoginUser> me(HttpSession session) {
        return Result.success(AuthHelper.requireLoginUser(session));
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpSession session) {
        session.invalidate();
        return Result.success("退出登录成功", null);
    }
}
