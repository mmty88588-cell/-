package com.blog.mapper;

import com.blog.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    User selectById(@Param("id") Long id);
    User selectByUsername(@Param("username") String username);
    int insert(User user);
    int updateById(User user);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
