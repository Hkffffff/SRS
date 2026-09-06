package com.example.srs.controller;

import com.example.srs.auth.AuthHelper;
import com.example.srs.auth.LoginUser;
import com.example.srs.common.Result;
import com.example.srs.domain.Blacklist;
import com.example.srs.service.BlacklistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/blacklist")
public class BlacklistController {

    private final BlacklistService blacklistService;

    public BlacklistController(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    /**
     * 检查当前学生是否处于黑名单封禁状态
     * 建议前端在路由守卫(Router Guard)或进入选座页面前调用
     */
    @GetMapping("/status")
    public Result<Boolean> checkStatus(HttpSession session) {
        LoginUser loginUser = AuthHelper.requireLoginUser(session);
        boolean isBlacklisted = blacklistService.isUserBlacklisted(loginUser.getStudentId());
        // 如果返回 true，前端可以直接弹窗提示并禁用预约操作
        return Result.success("查询状态成功", isBlacklisted);
    }

    /**
     * 获取学生的违约记录列表
     * 用于在前端“个人中心 -> 我的信誉”页面展示
     */
    @GetMapping("/records")
    public Result<List<Blacklist>> getRecords(HttpSession session) {
        LoginUser loginUser = AuthHelper.requireLoginUser(session);
        // 使用 LambdaQuery 按照违规时间倒序排列，最新的违规显示在最上面
        List<Blacklist> records = blacklistService.lambdaQuery()
                .eq(Blacklist::getStudentId, loginUser.getStudentId())
                .orderByDesc(Blacklist::getViolationDate)
                .list();
        return Result.success("查询记录成功", records);
    }
}
