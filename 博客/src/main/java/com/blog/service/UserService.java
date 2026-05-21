package com.blog.service;

import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    String login(LoginRequest request);
    User getCurrentUser(String token);
    void logout(String token);
    User getById(Long id);
    void updateProfile(User user);
    void updateStatus(Long id, Integer status);
}
