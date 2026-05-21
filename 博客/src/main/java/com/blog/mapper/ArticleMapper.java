package com.blog.mapper;

import com.blog.entity.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ArticleMapper {
    int insert(Article article);
    int updateById(Article article);
    int updateViewCount(@Param("id") Long id, @Param("count") Long count);
    int updateCommentCount(@Param("id") Long id, @Param("delta") int delta);
    int deleteById(@Param("id") Long id);
    Article selectById(@Param("id") Long id);
    List<Article> selectList(@Param("status") String status,
                             @Param("categoryId") Long categoryId,
                             @Param("tagId") Long tagId,
                             @Param("keyword") String keyword,
                             @Param("orderBy") String orderBy);
    List<Article> selectByUserId(@Param("userId") Long userId);
    List<Article> selectHot(@Param("limit") int limit);
    int insertArticleTags(@Param("articleId") Long articleId, @Param("tagIds") List<Long> tagIds);
    int deleteArticleTags(@Param("articleId") Long articleId);
    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);
}
