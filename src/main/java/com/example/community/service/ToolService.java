package com.example.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.community.dto.ToolDTO;
import com.example.community.entity.Tool;
import com.example.community.mapper.ToolMapper;
import com.example.community.util.UserContext;
import org.springframework.stereotype.Service;

@Service
public class ToolService {

    private final ToolMapper toolMapper;

    public ToolService(ToolMapper toolMapper) {
        this.toolMapper = toolMapper;
    }

    public void submit(ToolDTO dto) {
        Tool tool = new Tool();
        tool.setName(dto.getName());
        tool.setDescription(dto.getDescription());
        tool.setUrl(dto.getUrl());
        tool.setCategory(dto.getCategory());
        tool.setCoverUrl(dto.getCoverUrl());
        tool.setUserId(UserContext.getUserId());
        tool.setLikeCount(0);
        tool.setStatus(1);
        toolMapper.insert(tool);
    }

    public Page<Tool> list(int page, int size, String category) {
        LambdaQueryWrapper<Tool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tool::getStatus, 1);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Tool::getCategory, category);
        }
        wrapper.orderByDesc(Tool::getCreateTime);
        return toolMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
