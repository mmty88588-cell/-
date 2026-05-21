package com.blog.service.impl;

import com.blog.entity.Comment;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ArticleMapper articleMapper;

    @Override
    @Transactional
    public Comment create(Comment comment) {
        comment.setStatus(1);
        commentMapper.insert(comment);
        articleMapper.updateCommentCount(comment.getArticleId(), 1);
        return comment;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment != null) {
            commentMapper.deleteById(id);
            articleMapper.updateCommentCount(comment.getArticleId(), -1);
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        commentMapper.updateStatus(id, status);
    }

    @Override
    public List<Comment> getByArticleId(Long articleId) {
        return commentMapper.selectByArticleId(articleId);
    }

    @Override
    public List<Comment> getByUserId(Long userId) {
        return commentMapper.selectByUserId(userId);
    }
}
