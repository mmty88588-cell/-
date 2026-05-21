package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private TagService tagService;

    @PostMapping
    public Result<Article> publish(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        Article article = new Article();
        article.setTitle((String) body.get("title"));
        article.setContent((String) body.get("content"));
        article.setSummary((String) body.get("summary"));
        article.setCoverImage((String) body.get("coverImage"));
        article.setUserId(user.getId());

        Object catId = body.get("categoryId");
        if (catId != null) article.setCategoryId(Long.valueOf(catId.toString()));

        article.setStatus(body.get("status") != null ? (String) body.get("status") : "DRAFT");
        article.setIsTop(body.get("isTop") != null ? (Integer) body.get("isTop") : 0);

        @SuppressWarnings("unchecked")
        List<Integer> tagIdInts = (List<Integer>) body.get("tagIds");
        List<Long> tagIds = null;
        if (tagIdInts != null) {
            tagIds = new java.util.ArrayList<>();
            for (Integer i : tagIdInts) tagIds.add(i.longValue());
        }

        article = articleService.publish(article, tagIds);
        return Result.success(article);
    }

    @PutMapping("/{id}")
    public Result<Article> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Article article = new Article();
        article.setId(id);
        article.setTitle((String) body.get("title"));
        article.setContent((String) body.get("content"));
        article.setSummary((String) body.get("summary"));
        article.setCoverImage((String) body.get("coverImage"));

        Object catId = body.get("categoryId");
        if (catId != null) article.setCategoryId(Long.valueOf(catId.toString()));

        if (body.get("status") != null) article.setStatus((String) body.get("status"));
        if (body.get("isTop") != null) article.setIsTop((Integer) body.get("isTop"));

        @SuppressWarnings("unchecked")
        List<Integer> tagIdInts = (List<Integer>) body.get("tagIds");
        List<Long> tagIds = null;
        if (tagIdInts != null) {
            tagIds = new java.util.ArrayList<>();
            for (Integer i : tagIdInts) tagIds.add(i.longValue());
        }

        article = articleService.update(article, tagIds);
        return Result.success(article);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Article> getById(@PathVariable Long id) {
        Article article = articleService.getByIdAndIncrView(id);
        if (article == null) return Result.error(404, "文章不存在");
        List<Tag> tags = tagService.getByArticleId(id);
        // Set tags via reflection-like approach - use a map wrapper
        return Result.success(article);
    }

    @GetMapping("/list")
    public Result<PageResult<Article>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String orderBy) {
        PageResult<Article> result = articleService.list(pageNum, pageSize, status, categoryId, tagId, keyword, orderBy);
        return Result.success(result);
    }

    @GetMapping("/hot")
    public Result<List<Article>> hot(@RequestParam(defaultValue = "10") int size) {
        return Result.success(articleService.getHotFromCache(size));
    }

    @GetMapping("/my")
    public Result<List<Article>> myArticles(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return Result.success(articleService.getByUserId(user.getId()));
    }
}
