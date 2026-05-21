package com.blog.mapper;

import com.blog.entity.Tag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TagMapper {
    List<Tag> selectAll();
    List<Tag> selectByArticleId(@Param("articleId") Long articleId);
    Tag selectById(@Param("id") Long id);
    Tag selectByName(@Param("name") String name);
    int insert(Tag tag);
    int deleteById(@Param("id") Long id);
}
