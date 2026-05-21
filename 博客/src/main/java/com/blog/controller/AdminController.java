package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import com.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private CommentService commentService;

    @GetMapping("/users")
    public Result<List<Article>> users() {
        // Would need a user list method; showing article management as admin focus
        return Result.error("Use user management endpoints");
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @GetMapping("/articles")
    public Result<List<Article>> allArticles() {
        return Result.success(articleService.getByUserId(null));
    }

    @PutMapping("/articles/{id}/status")
    public Result<Void> updateArticleStatus(@PathVariable Long id, @RequestParam String status) {
        Article article = new Article();
        article.setId(id);
        article.setStatus(status);
        articleService.update(article, null);
        return Result.success();
    }

    @DeleteMapping("/articles/{id}")
    public Result<Void> adminDeleteArticle(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    @GetMapping("/comments")
    public Result<List<Comment>> allComments(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return Result.success(commentService.getByUserId(userId));
        }
        return Result.success(commentService.getByArticleId(null));
    }

    @PutMapping("/comments/{id}/status")
    public Result<Void> updateCommentStatus(@PathVariable Long id, @RequestParam Integer status) {
        commentService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> adminDeleteComment(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }

    @PostMapping("/sync-views")
    public Result<Void> syncViews() {
        articleService.syncViewCountToDb();
        return Result.success();
    }
}
