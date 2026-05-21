package com.blog.service.impl;

import com.blog.entity.Tag;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public List<Tag> listAll() {
        return tagMapper.selectAll();
    }

    @Override
    public List<Tag> getByArticleId(Long articleId) {
        return tagMapper.selectByArticleId(articleId);
    }

    @Override
    public Tag create(Tag tag) {
        Tag exist = tagMapper.selectByName(tag.getName());
        if (exist != null) {
            return exist;
        }
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public void delete(Long id) {
        tagMapper.deleteById(id);
    }
}
