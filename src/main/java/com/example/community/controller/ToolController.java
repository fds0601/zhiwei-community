package com.example.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.community.common.Result;
import com.example.community.dto.ToolDTO;
import com.example.community.entity.Tool;
import com.example.community.service.ToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI工具接口")
@RestController
@RequestMapping("/tool")
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @Operation(summary = "提交AI工具")
    @PostMapping
    public Result<Void> submit(@Valid @RequestBody ToolDTO dto) {
        toolService.submit(dto);
        return Result.success();
    }

    @Operation(summary = "工具列表（支持分类筛选）")
    @GetMapping("/list")
    public Result<Page<Tool>> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "12") int size,
                                   @RequestParam(required = false) String category) {
        return Result.success(toolService.list(page, size, category));
    }
}
