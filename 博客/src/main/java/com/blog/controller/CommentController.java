package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public Result<Comment> create(@RequestBody Comment comment, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        comment.setUserId(user.getId());
        comment.setParentId(comment.getParentId());
        return Result.success(commentService.create(comment));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }

    @GetMapping("/article/{articleId}")
    public Result<List<Comment>> getByArticle(@PathVariable Long articleId) {
        return Result.success(commentService.getByArticleId(articleId));
    }

    @GetMapping("/my")
    public Result<List<Comment>> getMyComments(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return Result.success(commentService.getByUserId(user.getId()));
    }
}
