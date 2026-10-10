package com.example.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.community.common.Result;
import com.example.community.entity.Notification;
import com.example.community.entity.User;
import com.example.community.mapper.NotificationMapper;
import com.example.community.mapper.UserMapper;
import com.example.community.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知接口")
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    public NotificationController(NotificationMapper notificationMapper, UserMapper userMapper) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
    }

    @Operation(summary = "我的通知列表")
    @GetMapping
    public Result<List<Notification>> list() {
        Long me = UserContext.getUserId();
        List<Notification> list = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, me)
                        .orderByDesc(Notification::getCreateTime).last("LIMIT 50"));
        for (Notification n : list) {
            User u = userMapper.selectById(n.getFromUserId());
            n.setFromNickname(u != null ? u.getNickname() : "系统");
        }
        return Result.success(list);
    }

    @Operation(summary = "未读通知数")
    @GetMapping("/unread")
    public Result<Long> unread() {
        Long me = UserContext.getUserId();
        long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, me)
                        .eq(Notification::getIsRead, 0));
        return Result.success(count);
    }

    @Operation(summary = "全部标记已读")
    @PostMapping("/read-all")
    public Result<Void> readAll() {
        Long me = UserContext.getUserId();
        Notification n = new Notification();
        n.setIsRead(1);
        notificationMapper.update(n, new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, me).eq(Notification::getIsRead, 0));
        return Result.success();
    }
}
