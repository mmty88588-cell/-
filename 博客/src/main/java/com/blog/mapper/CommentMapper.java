package com.blog.mapper;

import com.blog.entity.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper {
    int insert(Comment comment);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(@Param("id") Long id);
    Comment selectById(@Param("id") Long id);
    List<Comment> selectByArticleId(@Param("articleId") Long articleId);
    int countByArticleId(@Param("articleId") Long articleId);
    List<Comment> selectByUserId(@Param("userId") Long userId);
}
