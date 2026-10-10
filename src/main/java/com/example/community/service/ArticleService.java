package com.example.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.community.dto.ArticleDTO;
import com.example.community.dto.CommentDTO;
import com.example.community.entity.*;
import com.example.community.mapper.*;
import com.example.community.util.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleFavoriteMapper articleFavoriteMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final String CACHE_PREFIX = "article:";

    public ArticleService(ArticleMapper articleMapper, CommentMapper commentMapper,
                          ArticleLikeMapper articleLikeMapper,
                          ArticleFavoriteMapper articleFavoriteMapper,
                          CommentLikeMapper commentLikeMapper,
                          NotificationMapper notificationMapper,
                          UserMapper userMapper,
                          StringRedisTemplate redisTemplate) {
        this.articleMapper = articleMapper;
        this.commentMapper = commentMapper;
        this.articleLikeMapper = articleLikeMapper;
        this.articleFavoriteMapper = articleFavoriteMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    public void publish(ArticleDTO dto) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCategory(dto.getCategory());
        article.setTags(dto.getTags());
        article.setCoverUrl(dto.getCoverUrl());
        article.setUserId(UserContext.getUserId());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setStatus(1);
        articleMapper.insert(article);
    }

    public Map<String, Object> getById(Long id) {
        String cacheKey = CACHE_PREFIX + id;
        Article article = null;
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (json != null) {
                article = objectMapper.readValue(json, Article.class);
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，降级查数据库", e);
        }
        if (article == null) {
            article = articleMapper.selectById(id);
            if (article == null) throw new RuntimeException("文章不存在");
            try {
                redisTemplate.opsForValue().set(cacheKey,
                        objectMapper.writeValueAsString(article), Duration.ofMinutes(10));
            } catch (Exception e) {
                log.warn("Redis缓存写入失败", e);
            }
        }
        articleMapper.update(null,
                Wrappers.<Article>lambdaUpdate()
                        .eq(Article::getId, id)
                        .setSql("view_count = view_count + 1"));
        article.setViewCount(article.getViewCount() + 1);

        boolean liked = false, favorited = false;
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null) {
            liked = articleLikeMapper.selectCount(
                    new LambdaQueryWrapper<ArticleLike>()
                            .eq(ArticleLike::getArticleId, id)
                            .eq(ArticleLike::getUserId, currentUserId)) > 0;
            favorited = articleFavoriteMapper.selectCount(
                    new LambdaQueryWrapper<ArticleFavorite>()
                            .eq(ArticleFavorite::getArticleId, id)
                            .eq(ArticleFavorite::getUserId, currentUserId)) > 0;
        }

        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, id)
                        .orderByAsc(Comment::getCreateTime));
        Map<Long, String> nameMap = new HashMap<>();
        for (Comment c : comments) {
            nameMap.putIfAbsent(c.getUserId(),
                    userMapper.selectById(c.getUserId()) != null ? userMapper.selectById(c.getUserId()).getNickname() : "用户");
            if (c.getReplyToUserId() != null) {
                com.example.community.entity.User ru = userMapper.selectById(c.getReplyToUserId());
                nameMap.putIfAbsent(c.getReplyToUserId(), ru != null ? ru.getNickname() : "用户");
            }
        }
        for (Comment c : comments) {
            c.setNickname(nameMap.get(c.getUserId()));
            if (c.getReplyToUserId() != null) c.setReplyToNickname(nameMap.get(c.getReplyToUserId()));
            if (currentUserId != null) {
                c.setLiked(commentLikeMapper.selectCount(
                        new LambdaQueryWrapper<CommentLike>()
                                .eq(CommentLike::getCommentId, c.getId())
                                .eq(CommentLike::getUserId, currentUserId)) > 0);
            }
        }

        // 相关推荐：同标签取前5篇
        List<Article> related = new ArrayList<>();
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            String firstTag = article.getTags().split(",")[0].trim();
            related = articleMapper.selectList(
                    new LambdaQueryWrapper<Article>()
                            .select(Article::getId, Article::getTitle, Article::getCoverUrl)
                            .eq(Article::getStatus, 1)
                            .ne(Article::getId, id)
                            .like(Article::getTags, firstTag)
                            .orderByDesc(Article::getLikeCount)
                            .last("LIMIT 5"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("article", article);
        result.put("comments", comments);
        result.put("liked", liked);
        result.put("favorited", favorited);
        result.put("related", related);
        return result;
    }

    private static final java.util.function.Consumer<LambdaQueryWrapper<Article>> LIST_SELECT = w ->
            w.select(Article::getId, Article::getUserId, Article::getTitle, Article::getSummary,
                    Article::getCategory, Article::getTags, Article::getCoverUrl,
                    Article::getViewCount, Article::getLikeCount, Article::getStatus,
                    Article::getCreateTime, Article::getUpdateTime);

    private void fillNicknames(List<Article> list) {
        if (list == null || list.isEmpty()) return;
        List<Long> uids = list.stream().map(Article::getUserId).distinct().toList();
        Map<Long, String> nameMap = new HashMap<>();
        userMapper.selectBatchIds(uids).forEach(u -> nameMap.put(u.getId(), u.getNickname()));
        list.forEach(a -> a.setNickname(nameMap.getOrDefault(a.getUserId(), "用户" + a.getUserId())));
    }

    public Page<Article> list(int page, int size, String category, String tag) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        LIST_SELECT.accept(wrapper);
        wrapper.eq(Article::getStatus, 1);
        if (category != null && !category.isEmpty()) wrapper.eq(Article::getCategory, category);
        if (tag != null && !tag.isEmpty()) wrapper.like(Article::getTags, tag);
        wrapper.orderByDesc(Article::getCreateTime);
        Page<Article> p = articleMapper.selectPage(new Page<>(page, size), wrapper);
        fillNicknames(p.getRecords());
        return p;
    }

    public Page<Article> search(int page, int size, String keyword, String sort) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        LIST_SELECT.accept(wrapper);
        wrapper.eq(Article::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                    .or().like(Article::getSummary, keyword)
                    .or().like(Article::getTags, keyword));
        }
        if ("likes".equals(sort)) wrapper.orderByDesc(Article::getLikeCount);
        else if ("views".equals(sort)) wrapper.orderByDesc(Article::getViewCount);
        else wrapper.orderByDesc(Article::getCreateTime);
        Page<Article> p = articleMapper.selectPage(new Page<>(page, size), wrapper);
        fillNicknames(p.getRecords());
        return p;
    }

    public Page<Article> hot(int page, int size) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        LIST_SELECT.accept(wrapper);
        wrapper.eq(Article::getStatus, 1);
        wrapper.orderByDesc(Article::getLikeCount);
        Page<Article> p = articleMapper.selectPage(new Page<>(page, size), wrapper);
        fillNicknames(p.getRecords());
        return p;
    }

    public Page<Article> userArticles(Long userId, int page, int size) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        LIST_SELECT.accept(wrapper);
        wrapper.eq(Article::getUserId, userId);
        wrapper.eq(Article::getStatus, 1);
        wrapper.orderByDesc(Article::getCreateTime);
        Page<Article> p = articleMapper.selectPage(new Page<>(page, size), wrapper);
        fillNicknames(p.getRecords());
        return p;
    }

    public void update(Long id, ArticleDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new RuntimeException("文章不存在");
        if (!article.getUserId().equals(UserContext.getUserId()))
            throw new RuntimeException("只能修改自己的文章");
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCategory(dto.getCategory());
        article.setTags(dto.getTags());
        if (dto.getCoverUrl() != null) article.setCoverUrl(dto.getCoverUrl());
        articleMapper.updateById(article);
        redisTemplate.delete(CACHE_PREFIX + id);
    }

    public void delete(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new RuntimeException("文章不存在");
        if (!article.getUserId().equals(UserContext.getUserId())
                && !"ADMIN".equals(UserContext.getRole()))
            throw new RuntimeException("无权限删除");
        articleMapper.deleteById(id);
        redisTemplate.delete(CACHE_PREFIX + id);
    }

    @Transactional
    public String like(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new RuntimeException("文章不存在");
        Long userId = UserContext.getUserId();
        Long exists = articleLikeMapper.selectCount(
                new LambdaQueryWrapper<ArticleLike>()
                        .eq(ArticleLike::getArticleId, id)
                        .eq(ArticleLike::getUserId, userId));
        if (exists > 0) {
            articleLikeMapper.delete(
                    new LambdaQueryWrapper<ArticleLike>()
                            .eq(ArticleLike::getArticleId, id)
                            .eq(ArticleLike::getUserId, userId));
            articleMapper.update(null, Wrappers.<Article>lambdaUpdate()
                    .eq(Article::getId, id).setSql("like_count = GREATEST(like_count - 1, 0)"));
            redisTemplate.delete(CACHE_PREFIX + id);
            return "已取消点赞";
        }
        ArticleLike like = new ArticleLike();
        like.setArticleId(id);
        like.setUserId(userId);
        articleLikeMapper.insert(like);
        articleMapper.update(null, Wrappers.<Article>lambdaUpdate()
                .eq(Article::getId, id).setSql("like_count = like_count + 1"));
        redisTemplate.delete(CACHE_PREFIX + id);
        // 通知作者
        if (!article.getUserId().equals(userId)) {
            Notification n = new Notification();
            n.setUserId(article.getUserId());
            n.setFromUserId(userId);
            n.setType("LIKE");
            n.setArticleId(id);
            n.setContent("赞了你的文章《" + article.getTitle() + "》");
            notificationMapper.insert(n);
        }
        return "点赞成功";
    }

    public String favorite(Long id) {
        if (articleMapper.selectById(id) == null) throw new RuntimeException("文章不存在");
        Long userId = UserContext.getUserId();
        Long exists = articleFavoriteMapper.selectCount(
                new LambdaQueryWrapper<ArticleFavorite>()
                        .eq(ArticleFavorite::getArticleId, id)
                        .eq(ArticleFavorite::getUserId, userId));
        if (exists > 0) {
            articleFavoriteMapper.delete(new LambdaQueryWrapper<ArticleFavorite>()
                    .eq(ArticleFavorite::getArticleId, id).eq(ArticleFavorite::getUserId, userId));
            return "已取消收藏";
        }
        ArticleFavorite fav = new ArticleFavorite();
        fav.setArticleId(id);
        fav.setUserId(userId);
        articleFavoriteMapper.insert(fav);
        return "收藏成功";
    }

    @Transactional
    public void comment(Long id, CommentDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new RuntimeException("文章不存在");
        if (dto.getContent() == null || dto.getContent().trim().isEmpty())
            throw new RuntimeException("评论内容不能为空");
        Long userId = UserContext.getUserId();
        Comment comment = new Comment();
        comment.setArticleId(id);
        comment.setUserId(userId);
        comment.setContent(dto.getContent().trim());
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyToUserId(dto.getReplyToUserId());
        commentMapper.insert(comment);
        // 通知作者
        if (!article.getUserId().equals(userId)) {
            Notification n = new Notification();
            n.setUserId(article.getUserId());
            n.setFromUserId(userId);
            n.setType("COMMENT");
            n.setArticleId(id);
            n.setContent("评论了你的文章：" + dto.getContent().substring(0, Math.min(30, dto.getContent().length())));
            notificationMapper.insert(n);
        }
    }

    public void deleteComment(Long commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        Long uid = UserContext.getUserId();
        if (!c.getUserId().equals(uid) && !"ADMIN".equals(UserContext.getRole()))
            throw new RuntimeException("只能删除自己的评论");
        commentMapper.deleteById(commentId);
    }

    public String likeComment(Long commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        Long userId = UserContext.getUserId();
        Long exists = commentLikeMapper.selectCount(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUserId, userId));
        if (exists > 0) {
            commentLikeMapper.delete(new LambdaQueryWrapper<CommentLike>()
                    .eq(CommentLike::getCommentId, commentId).eq(CommentLike::getUserId, userId));
            commentMapper.update(null, Wrappers.<Comment>lambdaUpdate()
                    .eq(Comment::getId, commentId).setSql("like_count = GREATEST(like_count - 1, 0)"));
            return "已取消赞";
        }
        CommentLike cl = new CommentLike();
        cl.setCommentId(commentId);
        cl.setUserId(userId);
        commentLikeMapper.insert(cl);
        commentMapper.update(null, Wrappers.<Comment>lambdaUpdate()
                .eq(Comment::getId, commentId).setSql("like_count = like_count + 1"));
        return "点赞成功";
    }

    public Page<Article> myFavorites(int page, int size) {
        Long uid = UserContext.getUserId();
        List<ArticleFavorite> favs = articleFavoriteMapper.selectList(
                new LambdaQueryWrapper<ArticleFavorite>().eq(ArticleFavorite::getUserId, uid)
                        .orderByDesc(ArticleFavorite::getCreateTime));
        Page<Article> result = new Page<>(page, size);
        if (favs.isEmpty()) return result;
        List<Long> ids = favs.stream().map(ArticleFavorite::getArticleId).toList();
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        LIST_SELECT.accept(wrapper);
        wrapper.in(Article::getId, ids).eq(Article::getStatus, 1);
        Page<Article> p = articleMapper.selectPage(result, wrapper);
        fillNicknames(p.getRecords());
        return p;
    }

    // 个人主页统计
    public Map<String, Object> userStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        long articleCount = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId).eq(Article::getStatus, 1));
        long likeReceived = 0;
        List<Article> myArticles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().select(Article::getLikeCount)
                        .eq(Article::getUserId, userId).eq(Article::getStatus, 1));
        for (Article a : myArticles) likeReceived += (a.getLikeCount() != null ? a.getLikeCount() : 0);
        stats.put("articleCount", articleCount);
        stats.put("likeReceived", likeReceived);
        return stats;
    }
}
