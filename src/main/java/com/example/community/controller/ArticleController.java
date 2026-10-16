package com.example.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.community.common.Result;
import com.example.community.dto.ArticleDTO;
import com.example.community.dto.CommentDTO;
import com.example.community.entity.Article;
import com.example.community.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "文章接口")
@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public Result<Void> publish(@Valid @RequestBody ArticleDTO dto) {
        articleService.publish(dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.success(articleService.getById(id));
    }

    @GetMapping("/list")
    public Result<Page<Article>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String tag) {
        return Result.success(articleService.list(page, size, category, tag));
    }

    @GetMapping("/search")
    public Result<Page<Article>> search(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String sort) {
        return Result.success(articleService.search(page, size, keyword, sort));
    }

    @GetMapping("/hot")
    public Result<Page<Article>> hot(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String category) {
        return Result.success(articleService.hot(page, size, category));
    }

    @GetMapping("/follow/list")
    public Result<Page<Article>> follow(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String category) {
        return Result.success(articleService.followArticles(page, size, category));
    }

    @GetMapping("/user/{userId}")
    public Result<Page<Article>> userArticles(@PathVariable Long userId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.success(articleService.userArticles(userId, page, size));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleDTO dto) {
        articleService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<String> like(@PathVariable Long id) {
        return Result.success(articleService.like(id));
    }

    @PostMapping("/{id}/favorite")
    public Result<String> favorite(@PathVariable Long id) {
        return Result.success(articleService.favorite(id));
    }

    @PostMapping("/{id}/comment")
    public Result<Void> comment(@PathVariable Long id, @RequestBody CommentDTO dto) {
        articleService.comment(id, dto);
        return Result.success();
    }

    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        articleService.deleteComment(commentId);
        return Result.success();
    }

    @PostMapping("/comment/{commentId}/like")
    public Result<String> likeComment(@PathVariable Long commentId) {
        return Result.success(articleService.likeComment(commentId));
    }

    @GetMapping("/favorites")
    public Result<Page<Article>> favorites(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(articleService.myFavorites(page, size));
    }

    @GetMapping("/user/{userId}/stats")
    public Result<Map<String, Object>> userStats(@PathVariable Long userId) {
        return Result.success(articleService.userStats(userId));
    }
}
