package com.blog.service.impl;

import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${blog.redis.session-ttl}")
    private int sessionTtl;

    private static final String SESSION_PREFIX = "session:";

    @Override
    public User register(RegisterRequest request) {
        User exist = userMapper.selectByUsername(request.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setEmail(request.getEmail());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public String login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null || user.getStatus() == 0) {
            throw new RuntimeException("用户不存在或已被禁用");
        }
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        user.setPassword(null);
        redisTemplate.opsForValue().set(SESSION_PREFIX + token, user, sessionTtl, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public User getCurrentUser(String token) {
        if (token == null) return null;
        return (User) redisTemplate.opsForValue().get(SESSION_PREFIX + token);
    }

    @Override
    public void logout(String token) {
        if (token != null) {
            redisTemplate.delete(SESSION_PREFIX + token);
        }
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) user.setPassword(null);
        return user;
    }

    @Override
    public void updateProfile(User user) {
        userMapper.updateById(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
    }
}
