package com.blog.service;

import com.blog.entity.Tag;

import java.util.List;

public interface TagService {
    List<Tag> listAll();
    List<Tag> getByArticleId(Long articleId);
    Tag create(Tag tag);
    void delete(Long id);
}
