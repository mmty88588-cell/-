package com.blog.mapper;

import com.blog.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CategoryMapper {
    List<Category> selectAll();
    Category selectById(@Param("id") Long id);
    int insert(Category category);
    int updateById(Category category);
    int deleteById(@Param("id") Long id);
}
