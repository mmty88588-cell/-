package com.blog.service;

import com.blog.common.PageResult;
import com.blog.entity.Article;

import java.util.List;

public interface ArticleService {
    Article publish(Article article, List<Long> tagIds);
    Article update(Article article, List<Long> tagIds);
    void delete(Long id);
    Article getById(Long id);
    Article getByIdAndIncrView(Long id);
    PageResult<Article> list(int pageNum, int pageSize, String status, Long categoryId, Long tagId, String keyword, String orderBy);
    PageResult<Article> listHot(int pageNum, int pageSize);
    List<Article> getByUserId(Long userId);
    List<Article> getHotFromCache(int size);
    void syncViewCountToDb();
}
