package com.example.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.community.common.Result;
import com.example.community.entity.*;
import com.example.community.mapper.*;
import com.example.community.util.UserContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class V4Controller {

    private final DraftMapper draftMapper;
    private final ArticleReactionMapper reactionMapper;
    private final UserBlockMapper blockMapper;
    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final FollowMapper followMapper;
    private final StringRedisTemplate redis;
    private final PasswordEncoder passwordEncoder;

    public V4Controller(DraftMapper draftMapper, ArticleReactionMapper reactionMapper,
                        UserBlockMapper blockMapper, ReportMapper reportMapper, UserMapper userMapper,
                        ArticleMapper articleMapper, FollowMapper followMapper,
                        StringRedisTemplate redis, PasswordEncoder passwordEncoder) {
        this.draftMapper = draftMapper;
        this.reactionMapper = reactionMapper;
        this.blockMapper = blockMapper;
        this.reportMapper = reportMapper;
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
        this.followMapper = followMapper;
        this.redis = redis;
        this.passwordEncoder = passwordEncoder;
    }

    // ============ 草稿箱 ============
    @GetMapping("/draft/list")
    public Result<List<Draft>> draftList() {
        Long uid = UserContext.getUserId();
        List<Draft> list = draftMapper.selectList(
                new LambdaQueryWrapper<Draft>().eq(Draft::getUserId, uid)
                        .orderByDesc(Draft::getUpdateTime));
        return Result.success(list);
    }

    @PostMapping("/draft")
    public Result<Long> saveDraft(@RequestBody Draft draft) {
        draft.setId(null);
        draft.setUserId(UserContext.getUserId());
        draftMapper.insert(draft);
        return Result.success(draft.getId());
    }

    @PutMapping("/draft/{id}")
    public Result<Void> updateDraft(@PathVariable Long id, @RequestBody Draft draft) {
        Draft d = draftMapper.selectById(id);
        if (d == null || !d.getUserId().equals(UserContext.getUserId()))
            return Result.error(403, "无权限");
        draft.setId(id);
        draft.setUserId(UserContext.getUserId());
        draftMapper.updateById(draft);
        return Result.success(null);
    }

    @DeleteMapping("/draft/{id}")
    public Result<Void> deleteDraft(@PathVariable Long id) {
        Draft d = draftMapper.selectById(id);
        if (d != null && d.getUserId().equals(UserContext.getUserId()))
            draftMapper.deleteById(id);
        return Result.success(null);
    }

    // ============ 表情回应 ============
    @PostMapping("/article/{id}/react/{type}")
    public Result<Map<String, Object>> toggleReaction(@PathVariable Long id, @PathVariable String type) {
        Long uid = UserContext.getUserId();
        Long exists = reactionMapper.selectCount(
                new LambdaQueryWrapper<ArticleReaction>()
                        .eq(ArticleReaction::getArticleId, id)
                        .eq(ArticleReaction::getUserId, uid)
                        .eq(ArticleReaction::getType, type));
        if (exists > 0) {
            reactionMapper.delete(new LambdaQueryWrapper<ArticleReaction>()
                    .eq(ArticleReaction::getArticleId, id)
                    .eq(ArticleReaction::getUserId, uid)
                    .eq(ArticleReaction::getType, type));
        } else {
            ArticleReaction r = new ArticleReaction();
            r.setArticleId(id); r.setUserId(uid); r.setType(type);
            reactionMapper.insert(r);
        }
        // 返回各类型计数
        List<ArticleReaction> all = reactionMapper.selectList(
                new LambdaQueryWrapper<ArticleReaction>().eq(ArticleReaction::getArticleId, id));
        Map<String, Object> counts = new HashMap<>();
        counts.put("LIKE", all.stream().filter(r -> "LIKE".equals(r.getType())).count());
        counts.put("AWESOME", all.stream().filter(r -> "AWESOME".equals(r.getType())).count());
        counts.put("GOOD", all.stream().filter(r -> "GOOD".equals(r.getType())).count());
        Set<String> myTypes = all.stream().filter(r -> r.getUserId().equals(uid))
                .map(ArticleReaction::getType).collect(Collectors.toSet());
        counts.put("mine", myTypes);
        return Result.success(counts);
    }

    // ============ 屏蔽 ============
    @PostMapping("/block")
    public Result<Void> block(@RequestBody Map<String, String> body) {
        Long uid = UserContext.getUserId();
        String type = body.get("type");
        String value = body.get("value");
        Long exists = blockMapper.selectCount(new LambdaQueryWrapper<UserBlock>()
                .eq(UserBlock::getUserId, uid)
                .eq(UserBlock::getBlockType, type)
                .eq(UserBlock::getBlockValue, value));
        if (exists > 0) {
            blockMapper.delete(new LambdaQueryWrapper<UserBlock>()
                    .eq(UserBlock::getUserId, uid)
                    .eq(UserBlock::getBlockType, type)
                    .eq(UserBlock::getBlockValue, value));
        } else {
            UserBlock b = new UserBlock();
            b.setUserId(uid); b.setBlockType(type); b.setBlockValue(value);
            blockMapper.insert(b);
        }
        return Result.success(null);
    }

    @GetMapping("/block/list")
    public Result<List<UserBlock>> myBlocks() {
        return Result.success(blockMapper.selectList(
                new LambdaQueryWrapper<UserBlock>().eq(UserBlock::getUserId, UserContext.getUserId())));
    }

    // ============ 举报 ============
    @PostMapping("/report")
    public Result<Void> report(@RequestBody Map<String, Object> body) {
        Report r = new Report();
        r.setReporterId(UserContext.getUserId());
        if (body.get("articleId") != null) {
            r.setTargetType("ARTICLE");
            r.setTargetId(Long.valueOf(body.get("articleId").toString()));
            r.setArticleId(Long.valueOf(body.get("articleId").toString()));
        } else if (body.get("commentId") != null) {
            r.setTargetType("COMMENT");
            r.setTargetId(Long.valueOf(body.get("commentId").toString()));
            r.setCommentId(Long.valueOf(body.get("commentId").toString()));
        }
        r.setReason((String) body.get("reason"));
        r.setStatus("PENDING");
        reportMapper.insert(r);
        return Result.success(null);
    }

    @GetMapping("/admin/reports")
    public Result<List<Report>> listReports() {
        return Result.success(reportMapper.selectList(
                new LambdaQueryWrapper<Report>().orderByDesc(Report::getCreateTime)));
    }

    @PutMapping("/admin/reports/{id}/resolve")
    public Result<Void> resolveReport(@PathVariable Long id) {
        Report r = reportMapper.selectById(id);
        if (r != null) { r.setStatus("RESOLVED"); reportMapper.updateById(r); }
        return Result.success(null);
    }

    // ============ 修改密码 / 绑定手机 / 注销 ============
    @PutMapping("/user/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        Long uid = UserContext.getUserId();
        User u = userMapper.selectById(uid);
        if (!passwordEncoder.matches(body.get("oldPassword"), u.getPassword()))
            return Result.error(400, "原密码错误");
        u.setPassword(passwordEncoder.encode(body.get("newPassword")));
        userMapper.updateById(u);
        return Result.success(null);
    }

    @PutMapping("/user/phone")
    public Result<Void> bindPhone(@RequestBody Map<String, String> body) {
        User u = userMapper.selectById(UserContext.getUserId());
        u.setPhone(body.get("phone"));
        userMapper.updateById(u);
        return Result.success(null);
    }

    @DeleteMapping("/user/deactivate")
    public Result<Void> deactivate() {
        User u = userMapper.selectById(UserContext.getUserId());
        u.setStatus(0);
        userMapper.updateById(u);
        return Result.success(null);
    }

    // ============ 关注/粉丝列表 ============
    @GetMapping("/user/{id}/followers")
    public Result<List<Map<String, Object>>> followers(@PathVariable Long id) {
        // 谁关注了我：follow表中followId=id的，userId就是粉丝
        List<Follow> follows = followMapper.selectList(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, id));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Follow f : follows) {
            User u = userMapper.selectById(f.getUserId());
            if (u != null) {
                Map<String, Object> m = new HashMap<>();
                m.put("userId", u.getId());
                m.put("nickname", u.getNickname());
                m.put("avatar", u.getAvatar());
                m.put("bio", u.getBio());
                result.add(m);
            }
        }
        return Result.success(result);
    }

    @GetMapping("/user/{id}/following")
    public Result<List<Map<String, Object>>> following(@PathVariable Long id) {
        // 我关注了谁：follow表中userId=id的，followId就是我关注的人
        List<Follow> follows = followMapper.selectList(
                new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, id));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Follow f : follows) {
            User u = userMapper.selectById(f.getFollowId());
            if (u != null) {
                Map<String, Object> m = new HashMap<>();
                m.put("userId", u.getId());
                m.put("nickname", u.getNickname());
                m.put("avatar", u.getAvatar());
                m.put("bio", u.getBio());
                result.add(m);
            }
        }
        return Result.success(result);
    }

    // ============ 标签云 / 热门搜索 ============
    @GetMapping("/tags/hot")
    public Result<List<Map<String, Object>>> hotTags() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().select(Article::getId, Article::getTags).eq(Article::getStatus, 1));
        Map<String, Integer> tagCount = new HashMap<>();
        if (articles != null) {
            for (Article a : articles) {
                if (a == null || a.getTags() == null || a.getTags().isEmpty()) continue;
                for (String t : a.getTags().split(",")) {
                    t = t.trim();
                    if (!t.isEmpty()) tagCount.merge(t, 1, Integer::sum);
                }
            }
        }
        List<Map<String, Object>> result = tagCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .map(e -> { Map<String, Object> m = new HashMap<>(); m.put("tag", e.getKey()); m.put("count", e.getValue()); return m; })
                .collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/search/hot")
    public Result<List<String>> hotSearch() {
        // 从Redis取最近搜索词
        Set<String> keys = redis.keys("search:hot:*");
        List<String> result = new ArrayList<>();
        if (keys != null) {
            result = keys.stream()
                    .sorted((a, b) -> {
                        String ca = redis.opsForValue().get(a);
                        String cb = redis.opsForValue().get(b);
                        return Integer.compare(
                                cb != null ? Integer.parseInt(cb) : 0,
                                ca != null ? Integer.parseInt(ca) : 0);
                    })
                    .limit(10)
                    .map(k -> k.replace("search:hot:", ""))
                    .collect(Collectors.toList());
        }
        // 默认热词
        if (result.isEmpty()) result = List.of("Java", "Spring Boot", "LLM", "RAG", "Agent");
        return Result.success(result);
    }

    @GetMapping("/search/suggest")
    public Result<List<String>> suggest(@RequestParam String keyword) {
        // 记录搜索词到Redis
        String key = "search:hot:" + keyword;
        redis.opsForValue().increment(key);
        redis.expire(key, Duration.ofDays(7));
        // 从文章标题做联想
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .select(Article::getTitle)
                        .like(Article::getTitle, keyword)
                        .eq(Article::getStatus, 1)
                        .last("LIMIT 10"));
        return Result.success(articles.stream().map(Article::getTitle).collect(Collectors.toList()));
    }

    // ============ 全文搜索(标题+正文+标签+作者) ============
    @GetMapping("/search/all")
    public Result<?> fullSearch(@RequestParam String keyword,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<>();
        w.select(Article::getId, Article::getUserId, Article::getTitle, Article::getSummary,
                Article::getCategory, Article::getTags, Article::getCoverUrl,
                Article::getViewCount, Article::getLikeCount, Article::getCreateTime);
        w.eq(Article::getStatus, 1);
        w.and(q -> q.like(Article::getTitle, keyword)
                .or().like(Article::getContent, keyword)
                .or().like(Article::getTags, keyword));
        w.orderByDesc(Article::getCreateTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Article> p =
                articleMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), w);
        return Result.success(p);
    }

    // ============ 管理员：文章列表(含下架) ============
    @GetMapping("/admin/articles")
    public Result<List<Article>> adminArticles() {
        return Result.success(articleMapper.selectList(
                new LambdaQueryWrapper<Article>().orderByDesc(Article::getCreateTime)));
    }

    @PutMapping("/admin/article/{id}/offline")
    public Result<Void> offlineArticle(@PathVariable Long id) {
        Article a = articleMapper.selectById(id);
        if (a != null) {
            a.setStatus(a.getStatus() != null && a.getStatus() == 1 ? 0 : 1);
            articleMapper.updateById(a);
        }
        return Result.success(null);
    }

    @PutMapping("/admin/user/{id}/ban")
    public Result<Void> banUser(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u != null) { u.setStatus(0); userMapper.updateById(u); }
        return Result.success(null);
    }
}
