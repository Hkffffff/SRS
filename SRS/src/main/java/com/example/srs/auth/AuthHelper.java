package com.example.srs.auth;

import jakarta.servlet.http.HttpSession;

public final class AuthHelper {

    private AuthHelper() {
    }

    public static LoginUser getLoginUser(HttpSession session) {
        Object value = session.getAttribute(AuthConstants.LOGIN_USER);
        if (value instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    public static LoginUser requireLoginUser(HttpSession session) {
        LoginUser loginUser = getLoginUser(session);
        if (loginUser == null) {
            throw new IllegalStateException("未登录");
        }
        return loginUser;
    }

    public static boolean isAdmin(LoginUser loginUser) {
        return loginUser != null && loginUser.getRole() != null && loginUser.getRole() == 1;
    }
}
