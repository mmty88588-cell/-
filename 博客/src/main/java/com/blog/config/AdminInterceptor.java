package com.blog.config;

import com.blog.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = (User) request.getAttribute("currentUser");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":403,\"message\":\"需要管理员权限\"}");
            return false;
        }
        return true;
    }
}
