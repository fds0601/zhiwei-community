package com.example.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.community.common.Result;
import com.example.community.dto.LoginDTO;
import com.example.community.dto.RegisterDTO;
import com.example.community.entity.Follow;
import com.example.community.entity.Notification;
import com.example.community.entity.User;
import com.example.community.mapper.FollowMapper;
import com.example.community.mapper.NotificationMapper;
import com.example.community.mapper.UserMapper;
import com.example.community.service.UserService;
import com.example.community.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final FollowMapper followMapper;
    private final NotificationMapper notificationMapper;

    public UserController(UserService userService, UserMapper userMapper,
                          FollowMapper followMapper, NotificationMapper notificationMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.followMapper = followMapper;
        this.notificationMapper = notificationMapper;
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/{userId}")
    public Result<Map<String, Object>> info(@PathVariable Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new RuntimeException("用户不存在");
        Map<String, Object> m = new HashMap<>();
        m.put("userId", u.getId());
        m.put("nickname", u.getNickname());
        m.put("username", u.getUsername());
        m.put("bio", u.getBio());
        m.put("avatar", u.getAvatar());
        m.put("followerCount", followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId)));
        m.put("followCount", followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId)));
        Long me = UserContext.getUserId();
        if (me != null && !me.equals(userId)) {
            m.put("followed", followMapper.selectCount(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getUserId, me)
                            .eq(Follow::getFollowId, userId)) > 0);
        } else {
            m.put("followed", false);
        }
        return Result.success(m);
    }

    @Operation(summary = "关注/取消关注")
    @PostMapping("/{userId}/follow")
    public Result<String> follow(@PathVariable Long userId) {
        Long me = UserContext.getUserId();
        if (me.equals(userId)) throw new RuntimeException("不能关注自己");
        Long exists = followMapper.selectCount(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getUserId, me)
                        .eq(Follow::getFollowId, userId));
        if (exists > 0) {
            followMapper.delete(new LambdaQueryWrapper<Follow>()
                    .eq(Follow::getUserId, me).eq(Follow::getFollowId, userId));
            return Result.success("已取消关注");
        }
        Follow f = new Follow();
        f.setUserId(me);
        f.setFollowId(userId);
        followMapper.insert(f);
        // 通知被关注者
        Notification n = new Notification();
        n.setUserId(userId);
        n.setFromUserId(me);
        n.setType("FOLLOW");
        n.setContent("关注了你");
        notificationMapper.insert(n);
        return Result.success("关注成功");
    }

    @Operation(summary = "编辑个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> body) {
        Long me = UserContext.getUserId();
        User u = userMapper.selectById(me);
        if (body.get("nickname") != null) u.setNickname(body.get("nickname"));
        if (body.get("bio") != null) u.setBio(body.get("bio"));
        if (body.get("avatar") != null) u.setAvatar(body.get("avatar"));
        userMapper.updateById(u);
        return Result.success();
    }
}
