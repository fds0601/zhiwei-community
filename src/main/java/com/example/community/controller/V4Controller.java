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
    private final CommentMapper commentMapper;
    private final NotificationMapper notificationMapper;
    private final StringRedisTemplate redis;
    private final PasswordEncoder passwordEncoder;

    public V4Controller(DraftMapper draftMapper, ArticleReactionMapper reactionMapper,
                        UserBlockMapper blockMapper, ReportMapper reportMapper, UserMapper userMapper,
                        ArticleMapper articleMapper, FollowMapper followMapper,
                        CommentMapper commentMapper, NotificationMapper notificationMapper,
                        StringRedisTemplate redis, PasswordEncoder passwordEncoder) {
        this.draftMapper = draftMapper;
        this.reactionMapper = reactionMapper;
        this.blockMapper = blockMapper;
        this.reportMapper = reportMapper;
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
        this.followMapper = followMapper;
        this.commentMapper = commentMapper;
        this.notificationMapper = notificationMapper;
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
        Long userId = UserContext.getUserId();
        Long articleId = body.get("articleId") != null ? Long.valueOf(body.get("articleId").toString()) : null;
        Long commentId = body.get("commentId") != null ? Long.valueOf(body.get("commentId").toString()) : null;
        Long exists = reportMapper.selectCount(new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterId, userId)
                .eq(articleId != null, Report::getArticleId, articleId)
                .eq(commentId != null, Report::getCommentId, commentId)
                .eq(Report::getStatus, "PENDING"));
        if (exists > 0) throw new RuntimeException("您已举报过该内容，请勿重复举报");
        Report r = new Report();
        r.setReporterId(userId);
        if (articleId != null) {
            r.setTargetType("ARTICLE");
            r.setTargetId(articleId);
            r.setArticleId(articleId);
        } else if (commentId != null) {
            r.setTargetType("COMMENT");
            r.setTargetId(commentId);
            r.setCommentId(commentId);
        }
        r.setReason((String) body.get("reason"));
        r.setStatus("PENDING");
        reportMapper.insert(r);
        return Result.success(null);
    }

    @GetMapping("/admin/reports")
    public Result<List<Report>> listReports() {
        return Result.success(reportMapper.selectList(
                new LambdaQueryWrapper<Report>().orderByAsc(Report::getId)));
    }

    @PutMapping("/admin/reports/{id}/resolve")
    public Result<Void> resolveReport(@PathVariable Long id) {
        Report r = reportMapper.selectById(id);
        if (r != null && !"RESOLVED".equals(r.getStatus())) {
            r.setStatus("RESOLVED");
            reportMapper.updateById(r);
            Notification n = new Notification();
            n.setUserId(r.getReporterId());
            n.setFromUserId(1L);
            n.setType("SYSTEM");
            n.setContent("您的举报已经处理");
            notificationMapper.insert(n);
        }
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
        String phone = body.get("phone");
        if (phone != null && !phone.matches("^1[3-9]\\d{9}$"))
            return Result.error(400, "手机号格式不正确");
        User u = userMapper.selectById(UserContext.getUserId());
        u.setPhone(phone);
        userMapper.updateById(u);
        return Result.success(null);
    }

    @PutMapping("/user/email")
    public Result<Void> bindEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$"))
            return Result.error(400, "邮箱格式不正确");
        User exist = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
            .eq(User::getEmail, email).ne(User::getId, UserContext.getUserId()));
        if (exist != null) return Result.error(400, "该邮箱已被使用");
        User u = userMapper.selectById(UserContext.getUserId());
        u.setEmail(email);
        userMapper.updateById(u);
        return Result.success(null);
    }

    @PutMapping("/user/username")
    public Result<Void> changeUsername(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || !username.matches("^[a-zA-Z0-9_]{3,20}$"))
            return Result.error(400, "用户名需3-20位字母数字下划线");
        User exist = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
            .eq(User::getUsername, username));
        if (exist != null) return Result.error(400, "用户名已被占用");
        User u = userMapper.selectById(UserContext.getUserId());
        u.setUsername(username);
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
        return Result.success(List.of("Java", "Spring Boot", "MySQL", "Redis", "LLM", "RAG", "Agent", "微服务"));
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
                new LambdaQueryWrapper<Article>().orderByAsc(Article::getId)));
    }

    @PutMapping("/admin/article/{id}/offline")
    public Result<Void> offlineArticle(@PathVariable Long id) {
        Article a = articleMapper.selectById(id);
        if (a != null) {
            boolean wasOffline = a.getStatus() != null && a.getStatus() == 0;
            a.setStatus(wasOffline ? 1 : 0);
            articleMapper.updateById(a);
            Notification n = new Notification();
            n.setUserId(a.getUserId());
            n.setFromUserId(1L);
            n.setType("SYSTEM");
            n.setArticleId(id);
            n.setContent(wasOffline ? "您的文章《" + a.getTitle() + "》已恢复上架" : "您的文章《" + a.getTitle() + "》已被管理员下架");
            notificationMapper.insert(n);
        }
        return Result.success(null);
    }

    @PutMapping("/admin/user/{id}/ban")
    public Result<Void> banUser(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u != null) {
            u.setStatus(u.getStatus() != null && u.getStatus() == 1 ? 0 : 1);
            userMapper.updateById(u);
        }
        return Result.success(null);
    }

    @GetMapping("/admin/users")
    public Result<List<User>> adminUsers() {
        return Result.success(userMapper.selectList(
                new LambdaQueryWrapper<User>().orderByAsc(User::getId)));
    }

    @GetMapping("/admin/stats")
    public Result<Map<String, Object>> adminStats() {
        Map<String, Object> m = new HashMap<>();
        m.put("userCount", userMapper.selectCount(null));
        m.put("articleCount", articleMapper.selectCount(null));
        m.put("pendingReports", reportMapper.selectCount(
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, "PENDING")));
        m.put("commentCount", commentMapper.selectCount(null));
        return Result.success(m);
    }

    @GetMapping("/admin/comments")
    public Result<List<Comment>> adminComments() {
        return Result.success(commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().orderByAsc(Comment::getId)));
    }

    @DeleteMapping("/admin/comment/{id}")
    public Result<Void> delComment(@PathVariable Long id) {
        Comment c = commentMapper.selectById(id);
        if (c != null) {
            commentMapper.deleteById(id);
            Notification n = new Notification();
            n.setUserId(c.getUserId());
            n.setFromUserId(1L);
            n.setType("SYSTEM");
            n.setArticleId(c.getArticleId());
            n.setContent("您的评论因被举报已被管理员删除");
            notificationMapper.insert(n);
        }
        return Result.success(null);
    }

    @GetMapping("/admin/category-dist")
    public Result<List<Map<String, Object>>> categoryDist() {
        List<Article> all = articleMapper.selectList(null);
        Map<String, Long> countMap = new HashMap<>();
        for (Article a : all) {
            String c = a.getCategory() != null ? a.getCategory() : "未分类";
            countMap.merge(c, 1L, Long::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> e : countMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", e.getKey());
            item.put("count", e.getValue());
            result.add(item);
        }
        return Result.success(result);
    }

    @GetMapping("/admin/week-trend")
    public Result<List<Map<String, Object>>> weekTrend() {
        List<Article> all = articleMapper.selectList(null);
        Map<String, Long> countMap = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate d = java.time.LocalDate.now().minusDays(i);
            countMap.put(d.toString(), 0L);
        }
        for (Article a : all) {
            if (a.getCreateTime() != null) {
                String day = a.getCreateTime().toLocalDate().toString();
                countMap.merge(day, 1L, Long::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> e : countMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", e.getKey().substring(5));
            item.put("count", e.getValue());
            result.add(item);
        }
        return Result.success(result);
    }

    @GetMapping("/admin/user-trend")
    public Result<List<Map<String, Object>>> userTrend() {
        List<User> all = userMapper.selectList(null);
        Map<String, Long> countMap = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate d = java.time.LocalDate.now().minusDays(i);
            countMap.put(d.toString(), 0L);
        }
        for (User u : all) {
            if (u.getCreateTime() != null) {
                String day = u.getCreateTime().toLocalDate().toString();
                countMap.merge(day, 1L, Long::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> e : countMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", e.getKey().substring(5));
            item.put("count", e.getValue());
            result.add(item);
        }
        return Result.success(result);
    }
}
