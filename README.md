# 知微 · AI开发者全栈社区

基于 Spring Boot 3 + Vue3 的现代化内容社区系统，支持文章、评论、点赞、收藏、关注、通知、AI工具广场和管理后台。

## 技术栈

| 分类 | 技术 |
|------|------|
| 后端 | Spring Boot 3.4 / Spring Security / JWT |
| 前端 | Vue3 / Vite / Element Plus / ECharts |
| 持久层 | MyBatis-Plus 3.5 / MySQL 8 |
| 缓存 | Redis (Lettuce) |
| 认证 | JWT (jjwt 0.12) |
| 接口文档 | SpringDoc OpenAPI (Swagger UI) |
| 构建 | Maven / Vite |
| JDK | 21+ |
| Node | 18+ |

## 功能模块

### 用户系统
- 注册 / 登录，BCrypt 密码加密，JWT 无状态 Token
- 个人资料编辑、头像上传、修改密码、绑定手机/邮箱
- 关注 / 粉丝关系

### 文章系统
- 发布、编辑、删除文章（软删除）
- Markdown 编辑器，图片上传
- 分类筛选、标签、摘要
- 点赞（防重复）、收藏、浏览量统计（Redis 去重）
- 相关推荐（同分类相似文章）

### 互动系统
- 评论 / 嵌套回复
- 评论点赞
- 举报（文章 / 评论）
- 消息通知（点赞、收藏、评论、关注、系统通知）

### AI工具广场
- AI 工具提交、列表展示
- 分类筛选

### 管理后台
- 仪表盘：近7天文章趋势、用户增长趋势、分类分布饼图、最新举报/用户
- 用户管理：封禁 / 解封
- 文章管理：下架 / 恢复
- 评论管理：删除
- 举报处理：标记已处理
- 所有列表分页（每页10条）

### 工程化
- 全局异常处理、参数校验、统一返回体
- Swagger 接口文档
- Redis 缓存文章详情
- Vite 代理前后端分离开发

## 快速启动

### 1. 准备环境
- JDK 21+
- Node 18+
- MySQL 8
- Redis

### 2. 初始化数据库
```bash
mysql -u root -p < sql/init.sql
```

### 3. 后端配置
编辑 `src/main/resources/application.yml`，改为你自己的 MySQL 密码和 Redis 地址。

### 4. 启动后端
```bash
mvn spring-boot:run
```
后端运行在 http://localhost:8080

### 5. 启动前端
```bash
cd frontend
npm install
npm run dev
```
前端运行在 http://localhost:5173

### 6. 访问
- 前端页面：http://localhost:5173
- 接口文档：http://localhost:8080/swagger-ui.html

## 默认账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | ADMIN |
| zhangsan | 123456 | USER |
| lisi | 123456 | USER |
| wangwu | 123456 | USER |

## 项目结构

```
content-community/
├── src/main/java/com/example/community/
│   ├── CommunityApplication.java    # 启动类
│   ├── common/                      # 统一返回体、全局异常
│   ├── config/                      # Security、MyBatisPlus、OpenAPI、JWT过滤器
│   ├── controller/                  # 接口层
│   ├── service/                     # 业务层
│   ├── mapper/                      # 数据访问层
│   ├── entity/                      # 数据库实体
│   ├── dto/                         # 请求参数
│   └── util/                        # JWT、用户上下文工具
├── frontend/
│   ├── src/
│   │   ├── views/                   # 页面组件
│   │   ├── components/              # 通用组件
│   │   ├── router/                  # 路由
│   │   └── api/                     # API请求封装
│   └── package.json
├── sql/init.sql                     # 数据库初始化脚本
└── pom.xml
```
