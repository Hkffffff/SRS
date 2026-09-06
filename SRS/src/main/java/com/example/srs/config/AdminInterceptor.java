package com.example.srs.config;

import com.example.srs.auth.AuthHelper;
import com.example.srs.auth.LoginUser;
import com.example.srs.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        LoginUser loginUser = session == null ? null : AuthHelper.getLoginUser(session);
        if (loginUser == null) {
            writeJson(response, 401, Result.unauthorized("请先登录"));
            return false;
        }
        if (AuthHelper.isAdmin(loginUser)) {
            return true;
        }

        writeJson(response, 403, Result.forbidden("无管理员权限"));
        return false;
    }

    private void writeJson(HttpServletResponse response, int status, Result<String> body) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
