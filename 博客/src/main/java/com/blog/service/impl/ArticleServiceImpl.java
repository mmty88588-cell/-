package com.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.blog.common.PageResult;
import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import com.blog.service.ArticleService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${blog.redis.article-ttl}")
    private int articleTtl;
    @Value("${blog.redis.hot-articles-size}")
    private int hotArticlesSize;

    private static final String ARTICLE_CACHE_PREFIX = "article:";
    private static final String HOT_ARTICLES_KEY = "hot_articles";
    private static final String ARTICLE_LIST_CACHE_PREFIX = "article_list:";

    @Override
    @Transactional
    public Article publish(Article article, List<Long> tagIds) {
        article.setViewCount(0L);
        article.setCommentCount(0);
        articleMapper.insert(article);
        if (tagIds != null && !tagIds.isEmpty()) {
            articleMapper.insertArticleTags(article.getId(), tagIds);
        }
        return article;
    }

    @Override
    @Transactional
    public Article update(Article article, List<Long> tagIds) {
        articleMapper.updateById(article);
        if (tagIds != null) {
            articleMapper.deleteArticleTags(article.getId());
            if (!tagIds.isEmpty()) {
                articleMapper.insertArticleTags(article.getId(), tagIds);
            }
        }
        deleteArticleCache(article.getId());
        return article;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        articleMapper.deleteArticleTags(id);
        articleMapper.deleteById(id);
        deleteArticleCache(id);
        redisTemplate.opsForZSet().remove(HOT_ARTICLES_KEY, id.toString());
    }

    @Override
    public Article getById(Long id) {
        String cacheKey = ARTICLE_CACHE_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return JSON.parseObject(cached.toString(), Article.class);
        }
        Article article = articleMapper.selectById(id);
        if (article != null) {
            redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(article), articleTtl, TimeUnit.SECONDS);
        }
        return article;
    }

    @Override
    public Article getByIdAndIncrView(Long id) {
        Article article = getById(id);
        if (article != null) {
            redisTemplate.opsForZSet().incrementScore(HOT_ARTICLES_KEY, id.toString(), 1);
            article.setViewCount(article.getViewCount() + 1);
            redisTemplate.opsForValue().set(ARTICLE_CACHE_PREFIX + id, JSON.toJSONString(article), articleTtl, TimeUnit.SECONDS);
        }
        return article;
    }

    @Override
    public PageResult<Article> list(int pageNum, int pageSize, String status, Long categoryId, Long tagId, String keyword, String orderBy) {
        if (keyword == null && categoryId == null && tagId == null && "hot".equals(orderBy)) {
            return listHot(pageNum, pageSize);
        }

        String cacheKey = ARTICLE_LIST_CACHE_PREFIX + status + "_" + categoryId + "_" + tagId + "_" + keyword + "_" + orderBy + "_" + pageNum + "_" + pageSize;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return JSON.parseObject(cached.toString(), PageResult.class);
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Article> list = articleMapper.selectList(status, categoryId, tagId, keyword, orderBy);
        PageInfo<Article> pageInfo = new PageInfo<>(list);
        PageResult<Article> result = new PageResult<>(pageInfo.getTotal(), pageNum, pageSize, list);

        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), 60, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public PageResult<Article> listHot(int pageNum, int pageSize) {
        Set<ZSetOperations.TypedTuple<Object>> hotSet = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_ARTICLES_KEY, 0, hotArticlesSize - 1);

        if (hotSet == null || hotSet.isEmpty()) {
            List<Article> dbHot = articleMapper.selectHot(hotArticlesSize);
            for (Article a : dbHot) {
                redisTemplate.opsForZSet().add(HOT_ARTICLES_KEY, a.getId().toString(), a.getViewCount());
            }
            int from = (pageNum - 1) * pageSize;
            int to = Math.min(from + pageSize, dbHot.size());
            List<Article> page = from < dbHot.size() ? dbHot.subList(from, to) : new ArrayList<>();
            return new PageResult<>((long) dbHot.size(), pageNum, pageSize, page);
        }

        List<Long> hotIds = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : hotSet) {
            hotIds.add(Long.valueOf(tuple.getValue().toString()));
        }

        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, hotIds.size());
        if (from >= hotIds.size()) {
            return new PageResult<>((long) hotIds.size(), pageNum, pageSize, new ArrayList<>());
        }

        List<Long> pageIds = hotIds.subList(from, to);
        List<Article> articles = new ArrayList<>();
        for (Long id : pageIds) {
            Article a = getById(id);
            if (a != null) articles.add(a);
        }
        return new PageResult<>((long) hotIds.size(), pageNum, pageSize, articles);
    }

    @Override
    public List<Article> getByUserId(Long userId) {
        return articleMapper.selectByUserId(userId);
    }

    @Override
    public List<Article> getHotFromCache(int size) {
        Set<ZSetOperations.TypedTuple<Object>> hotSet = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_ARTICLES_KEY, 0, size - 1);
        List<Article> articles = new ArrayList<>();
        if (hotSet != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : hotSet) {
                Article a = getById(Long.valueOf(tuple.getValue().toString()));
                if (a != null) articles.add(a);
            }
        }
        if (articles.isEmpty()) {
            articles = articleMapper.selectHot(size);
        }
        return articles;
    }

    @Scheduled(fixedRate = 300000)
    public void syncViewCountToDb() {
        Set<ZSetOperations.TypedTuple<Object>> hotSet = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_ARTICLES_KEY, 0, -1);
        if (hotSet != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : hotSet) {
                Long articleId = Long.valueOf(tuple.getValue().toString());
                long viewCount = tuple.getScore() != null ? tuple.getScore().longValue() : 0;
                articleMapper.updateViewCount(articleId, viewCount);
            }
        }
    }

    private void deleteArticleCache(Long id) {
        redisTemplate.delete(ARTICLE_CACHE_PREFIX + id);
        Set<String> keys = redisTemplate.keys(ARTICLE_LIST_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
