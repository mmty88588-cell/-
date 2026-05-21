package com.blog.service;

import com.blog.entity.Comment;

import java.util.List;

public interface CommentService {
    Comment create(Comment comment);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    List<Comment> getByArticleId(Long articleId);
    List<Comment> getByUserId(Long userId);
}
