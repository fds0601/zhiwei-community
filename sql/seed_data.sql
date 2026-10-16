-- 清空所有表
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notification;
TRUNCATE TABLE article_reaction;
TRUNCATE TABLE comment_like;
TRUNCATE TABLE article_like;
TRUNCATE TABLE article_favorite;
TRUNCATE TABLE comment;
TRUNCATE TABLE follow;
TRUNCATE TABLE draft;
TRUNCATE TABLE report;
TRUNCATE TABLE user_block;
TRUNCATE TABLE search_hotword;
TRUNCATE TABLE tool;
TRUNCATE TABLE article;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入用户（密码统一123456，BCrypt加密）
INSERT INTO user (id, username, password, nickname, bio, email, role, status) VALUES
(1, 'admin', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', '管理员', '知微社区管理员', 'admin@zhiwei.com', 'ADMIN', 1),
(2, 'zhangsan', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', '张三', 'Java后端开发，关注AI应用', 'zhangsan@qq.com', 'USER', 1),
(3, 'lisi', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', '李四', '前端工程师，热爱Vue和React', 'lisi@qq.com', 'USER', 1),
(4, 'wangwu', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', '王五', '算法工程师，RAG方向', 'wangwu@qq.com', 'USER', 1),
(5, 'zhaoliu', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', '赵六', '全栈开发，微服务爱好者', 'zhaoliu@qq.com', 'USER', 1),
(6, 'qianqi', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', '钱七', '运维开发，DevOps实践者', 'qianqi@qq.com', 'USER', 1);

-- 插入30篇文章
INSERT INTO article (user_id, title, summary, content, category, tags, view_count, like_count, status) VALUES
(2, 'Spring Boot 3 整合 MyBatis-Plus 实战指南', '从配置到CRUD，完整演示Spring Boot 3整合MyBatis-Plus的全过程', '# 前言\nMyBatis-Plus是MyBatis的增强工具，本文带你从零开始整合。\n\n## 1. 引入依赖\n```xml\n<dependency>\n  <groupId>com.baomidou</groupId>\n  <artifactId>mybatis-plus-boot-starter</artifactId>\n  <version>3.5.5</version>\n</dependency>\n```\n\n## 2. 配置数据源\n在application.yml中配置MySQL连接。\n\n## 3. 创建Mapper接口\n继承BaseMapper即可获得CRUD能力。\n\n## 4. 分页查询\n配置分页插件后即可使用。\n\n## 总结\nMP极大简化了数据库操作，推荐使用。', '后端开发', 'Java,Spring Boot,MyBatis', 320, 25, 1),
(3, 'Vue3 Composition API 最佳实践', '深入理解setup、ref、reactive的使用场景和避坑指南', '# Vue3 Composition API\n\n## ref vs reactive\n- ref适用于基本类型\n- reactive适用于对象\n\n## 避坑点\n解构reactive会丢失响应式，需要用toRefs。\n\n## 生命周期\nonMounted、onUnmounted等钩子函数的使用。', '前端开发', 'Vue3,JavaScript,前端', 280, 18, 1),
(4, 'RAG检索增强生成：从原理到落地', '详解RAG架构，包括文档切分、向量化、检索、重排序全流程', '# RAG检索增强生成\n\n## 什么是RAG\nRAG（Retrieval-Augmented Generation）通过检索外部知识库来增强大模型回答的准确性。\n\n## 核心流程\n1. 文档加载与切分\n2. Embedding向量化\n3. 存入向量数据库\n4. 用户问题检索相关片段\n5. 拼接上下文送入LLM生成回答\n\n## 常见问题\n- 切分策略影响召回率\n- 向量数据库选择Milvus/Qdrant\n- 需要重排序提升精度', 'AI应用', 'RAG,大模型,向量数据库', 450, 42, 1),
(5, 'Docker部署Spring Boot应用完整教程', '多阶段构建、Dockerfile编写、容器编排全流程', '# Docker部署Spring Boot\n\n## Dockerfile示例\n```dockerfile\nFROM maven:3.9 AS build\nCOPY . .\nRUN mvn package -DskipTests\n\nFROM openjdk:21-jre\nCOPY --from=build/target/*.jar app.jar\nEXPOSE 8080\nENTRYPOINT ["java","-jar","/app.jar"]\n```\n\n## 常用命令\n- docker build -t myapp .\n- docker run -p 8080:8080 myapp', '运维部署', 'Docker,DevOps,Spring Boot', 190, 12, 1),
(2, 'MySQL索引优化实战：Explain分析慢查询', '通过Explain执行计划分析SQL，优化索引设计', '# MySQL索引优化\n\n## Explain关键字段\n- type：访问类型，ALL表示全表扫描\n- key：实际使用的索引\n- rows：扫描行数\n\n## 常见优化\n1. 联合索引最左前缀原则\n2. 避免select *\n3. 用覆盖索引避免回表\n\n## 案例\n一条慢查询从3秒优化到50毫秒。', '后端开发', 'MySQL,数据库,索引优化', 380, 30, 1),
(4, 'LangChain入门：构建你的第一个AI应用', '用LangChain快速搭建一个带记忆的对话机器人', '# LangChain入门\n\n## 安装\n```bash\npip install langchain openai\n```\n\n## 第一个程序\n```python\nfrom langchain.chat_models import ChatOpenAI\nfrom langchain.chains import ConversationChain\n\nllm = ChatOpenAI(model_name=\"gpt-4\")\nchain = ConversationChain(llm=llm)\nprint(chain.predict(input=\"你好\"))\n```\n\n## 核心概念\n- Chain：组合多个组件\n- Memory：对话记忆\n- Prompt：提示模板', 'AI应用', 'LangChain,Python,大模型', 260, 20, 1),
(3, 'CSS Flexbox布局完全指南', '从容器到项目属性，Flexbox一篇吃透', '# Flexbox布局\n\n## 容器属性\n- display: flex\n- flex-direction: row/column\n- justify-content: 主轴对齐\n- align-items: 交叉轴对齐\n- flex-wrap: 换行\n\n## 项目属性\n- flex-grow: 放大比例\n- flex-shrink: 缩小比例\n- flex-basis: 初始尺寸\n\n## 常见布局\n居中、等分、圣杯布局都能轻松实现。', '前端开发', 'CSS,前端,布局', 150, 8, 1),
(5, 'Redis缓存穿透、击穿、雪崩解决方案', '三大缓存问题的原理和应对策略', '# Redis缓存三大问题\n\n## 缓存穿透\n查询不存在的数据，请求直接打到数据库。\n解决：布隆过滤器、缓存空值。\n\n## 缓存击穿\n热点key过期，大量请求同时打DB。\n解决：互斥锁、逻辑过期。\n\n## 缓存雪崩\n大量key同时过期。\n解决：过期时间加随机值、多级缓存。', '后端开发', 'Redis,缓存,高并发', 420, 35, 1),
(2, 'Spring Security + JWT 实现登录鉴权', '完整实现无状态登录认证，含Token刷新机制', '# JWT鉴权\n\n## 流程\n1. 用户登录验证账号密码\n2. 服务端生成JWT返回客户端\n3. 后续请求Header携带Token\n4. 拦截器校验Token有效性\n\n## 实现要点\n- PasswordEncoder用BCrypt\n- Token设置过期时间\n- 需要黑名单处理注销\n\n## 代码\nJwtUtil工具类负责生成和解析。', '后端开发', 'Spring Security,JWT,认证授权', 310, 22, 1),
(4, '什么是AI Agent？从概念到框架对比', '解析Agent核心组成，对比LangChain Agent、AutoGPT、Dify', '# AI Agent\n\n## Agent三要素\n- Planning：任务规划与分解\n- Memory：短期/长期记忆\n- Action：调用工具执行\n\n## 主流框架\n- LangChain Agent：灵活定制\n- Dify：低代码可视化\n- AutoGPT：自主任务执行\n\n## 应用场景\n智能客服、代码助手、数据分析。', 'AI应用', 'Agent,大模型,AI框架', 390, 38, 1),
(6, 'Nginx反向代理配置详解', '负载均衡、HTTPS、静态资源缓存一篇搞定', '# Nginx配置\n\n## 反向代理\n```nginx\nserver {\n  listen 80;\n  location / {\n    proxy_pass http://localhost:8080;\n  }\n}\n```\n\n## 负载均衡\nip_hash、round-robin策略。\n\n## HTTPS配置\n申请免费证书，配置ssl_certificate。', '运维部署', 'Nginx,反向代理,负载均衡', 170, 10, 1),
(3, 'React Hooks常见误区与最佳实践', 'useEffect依赖数组、闭包陷阱、性能优化', '# React Hooks\n\n## useEffect注意事项\n- 依赖数组不要漏写\n- 不要在依赖里放对象/函数\n- 需要清理函数处理副作用\n\n## 闭包陷阱\nesetState后立即读不到新值。\n\n## 性能优化\nuseMemo、useCallback避免不必要渲染。', '前端开发', 'React,Hooks,前端', 200, 14, 1),
(2, '消息队列Kafka入门与应用场景', '为什么用MQ？削峰填谷、异步解耦', '# Kafka入门\n\n## 为什么用消息队列\n- 异步处理：发短信、发邮件异步化\n- 削峰填谷：秒杀场景缓冲流量\n- 系统解耦\n\n## 核心概念\nBroker、Topic、Partition、Consumer Group。\n\n## 简单示例\n生产消息、消费消息的Java代码。', '后端开发', 'Kafka,消息队列,高并发', 280, 16, 1),
(4, '向量数据库选型：Milvus vs Qdrant vs Chroma', '对比三大向量数据库的性能、易用性和适用场景', '# 向量数据库对比\n\n## Milvus\n功能最全，分布式，适合生产大规模场景。\n\n## Qdrant\nRust编写，性能好，过滤功能强。\n\n## Chroma\n轻量级，适合原型开发和小规模应用。\n\n## 选型建议\n生产用Milvus，快速验证用Chroma。', 'AI应用', '向量数据库,RAG,Milvus', 230, 19, 1),
(5, 'Git常用命令速查与工作流', 'Git Flow、变基、冲突处理一文搞定', '# Git命令\n\n## 日常操作\ngit add、commit、push、pull。\n\n## 分支管理\ngit branch、checkout、merge。\n\n## 变基\ngit rebase保持提交历史整洁。\n\n## 冲突处理\n手动编辑冲突文件后add。', '工程效率', 'Git,版本控制,工具', 120, 6, 1),
(2, 'RESTful API设计规范与最佳实践', 'URL设计、HTTP方法、状态码、版本管理', '# RESTful API设计\n\n## URL设计\n名词复数，如/api/users。\n\n## HTTP方法\nGET查询、POST创建、PUT更新、DELETE删除。\n\n## 状态码\n200成功、400参数错误、401未登录、403无权限、500服务器错误。\n\n## 版本管理\n/api/v1/users。', '后端开发', 'API设计,RESTful,后端', 180, 11, 1),
(4, 'Prompt Engineering：写好提示词的10个技巧', '从角色设定到思维链，提升大模型输出质量', '# Prompt技巧\n\n1. 指定角色：你是一个资深Java工程师\n2. 给出示例：Few-shot prompting\n3. 思维链：让模型一步步思考\n4. 限定输出格式：JSON/Markdown\n5. 拆分复杂任务\n6. 指定约束条件\n7. 提供上下文背景\n8. 迭代优化\n9. 负面提示词\n10. 温度参数调节', 'AI应用', 'Prompt,大模型,提示词', 350, 32, 1),
(3, 'TypeScript入门：从JS到TS的平滑迁移', '类型系统、接口、泛型实战', '# TypeScript\n\n## 基础类型\nstring、number、boolean、array。\n\n## 接口interface\n描述对象结构。\n\n## 泛型\n编写可复用的通用组件。\n\n## 迁移策略\n逐步迁移，从.js改为.ts。', '前端开发', 'TypeScript,前端,JavaScript', 160, 9, 1),
(5, 'Spring Boot微服务拆分实践', '单体到微服务的演进路径与注意事项', '# 微服务拆分\n\n## 拆分原则\n- 按业务领域拆分\n- 数据库独立\n- 服务间通过REST/RPC通信\n\n## 常见坑\n- 分布式事务问题\n- 服务间循环依赖\n- 链路追踪复杂\n\n## 建议\n先模块化，再拆服务。', '后端开发', '微服务,Spring Cloud,架构', 290, 20, 1),
(6, 'Linux常用命令与Shell脚本入门', '日常运维必会命令汇总', '# Linux命令\n\n## 文件操作\nls、cd、cp、mv、rm、find。\n\n## 文本处理\ncat、grep、awk、sed。\n\n## 系统监控\ntop、df、free、netstat。\n\n## Shell脚本\n简单的批量处理脚本示例。', '运维部署', 'Linux,Shell,运维', 140, 7, 1),
(2, 'MyBatis-Plus分页插件配置与原理', '分页不生效？看完这篇就懂', '# MP分页插件\n\n## 配置\n```java\n@Configuration\npublic class MybatisConfig {\n  @Bean\n  public MybatisPlusInterceptor mybatisPlusInterceptor() {\n    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();\n    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));\n    return interceptor;\n  }\n}\n```\n\n## 原理\n拦截SQL，自动追加LIMIT。', '后端开发', 'MyBatis,Java,数据库', 130, 5, 1),
(4, '大模型微调入门：LoRA原理与实践', '为什么微调？LoRA如何低成本适配垂直领域', '# 大模型微调\n\n## 为什么微调\n通用模型在垂直领域表现不佳，需要领域数据微调。\n\n## LoRA原理\n冻结原模型权重，只训练低秩矩阵，大幅减少参数量。\n\n## 实践流程\n1. 准备数据集\n2. 选择基座模型\n3. 配置LoRA参数\n4. 训练与合并', 'AI应用', '大模型,微调,LoRA', 310, 28, 1),
(3, '前端性能优化：从加载到渲染全链路', '资源压缩、懒加载、重排重绘优化', '# 前端性能\n\n## 加载优化\n- 压缩资源\n- CDN加速\n- 图片懒加载\n\n## 渲染优化\n- 减少重排重绘\n- 防抖节流\n- 虚拟列表\n\n## 测量工具\nLighthouse、Chrome DevTools Performance。', '前端开发', '性能优化,前端,Web', 190, 13, 1),
(5, '什么是分布式事务？CAP与BASE理论', '分布式系统的一致性难题与解决方案', '# 分布式事务\n\n## CAP理论\n一致性、可用性、分区容忍，三者不可兼得。\n\n## BASE理论\n基本可用、软状态、最终一致性。\n\n## 解决方案\n- 两阶段提交2PC\n- TCC补偿事务\n- 本地消息表\n- Seata框架', '后端开发', '分布式,架构,微服务', 220, 15, 1),
(4, 'Dify低代码平台搭建AI工作流实战', '可视化编排AI应用，无需写代码', '# Dify实战\n\n## 什么是Dify\n开源LLM应用开发平台，可视化编排工作流。\n\n## 核心功能\n- Prompt编排\n- 知识库RAG\n- 工具调用\n- Agent工作流\n\n## 快速开始\nDocker部署后创建应用，拖拽节点编排。', 'AI应用', 'Dify,低代码,AI工作流', 270, 24, 1),
(2, 'Java 21新特性：虚拟线程详解', '虚拟线程如何简化高并发编程', '# Java 21虚拟线程\n\n## 传统线程问题\n平台线程重量级，高并发下内存压力大。\n\n## 虚拟线程\nJVM管理，轻量级，百万级并发轻松实现。\n\n## 示例\n```java\nThread.startVirtualThread(() -> {\n  System.out.println(\"Hello\");\n});\n```', '后端开发', 'Java21,虚拟线程,高并发', 240, 17, 1),
(6, 'CI/CD流水线搭建：GitHub Actions实战', '从代码提交到自动部署', '# GitHub Actions\n\n## 配置文件\n.github/workflows/deploy.yml\n\n## 流程\n1. push触发\n2. 安装依赖\n3. 运行测试\n4. 构建镜像\n5. 部署到服务器\n\n## Secrets配置\n服务器SSH密钥。', '运维部署', 'CI/CD,GitHub Actions,DevOps', 160, 9, 1),
(3, 'JavaScript异步编程：Promise与async/await', '从回调地狱到优雅异步', '# JS异步\n\n## 回调地狱\n嵌套层级深，难以维护。\n\n## Promise\n链式调用，then/catch。\n\n## async/await\n同步写法，异步执行。\n\n## 错误处理\ntry/catch包裹。', '前端开发', 'JavaScript,异步编程,前端', 110, 6, 1),
(5, 'MySQL主从复制与读写分离', '高可用数据库架构入门', '# MySQL主从\n\n## 原理\nBinlog同步，主库写，从库读。\n\n## 配置\n主库开启binlog，从库配置连接。\n\n## 读写分离\n应用层用Sharding-JDBC或代理层ProxySQL。', '后端开发', 'MySQL,高可用,数据库架构', 180, 12, 1),
(4, '大模型应用架构：从单体到RAG+Agent演进', '总结AI应用的技术选型路线', '# AI应用架构演进\n\n## 阶段一：直接调API\n简单场景直接调用大模型API。\n\n## 阶段二：RAG\n接入知识库，解决幻觉问题。\n\n## 阶段三：Agent\n让模型自主调用工具完成复杂任务。\n\n## 阶段四：多Agent协作\n多个Agent分工协作。', 'AI应用', 'AI架构,RAG,Agent', 330, 30, 1);

-- 插入关注关系（用户2-6互相关注）
INSERT INTO follow (user_id, follow_id) VALUES
(2,3),(2,4),(2,5),(2,6),
(3,2),(3,4),(3,5),
(4,2),(4,3),(4,5),(4,6),
(5,2),(5,4),(5,6),
(6,2),(6,4);

-- 插入点赞（每篇文章3-5个赞）
INSERT INTO article_like (article_id, user_id) VALUES
(1,2),(1,3),(1,4),(1,5),
(2,3),(2,4),(2,6),
(3,2),(3,4),(3,5),(3,6),
(4,5),(4,6),
(5,2),(5,4),(5,6),
(6,2),(6,3),(6,4),
(7,3),(7,5),
(8,2),(8,4),(8,6),
(9,2),(9,5),(9,6),
(10,2),(10,3),(10,4),(10,5),
(11,5),(11,6),
(12,3),(12,4),
(13,2),(13,5),(13,6),
(14,4),(14,5),
(15,5),(15,6),
(16,2),(16,3),
(17,2),(17,4),(17,6),
(18,3),(18,4),
(19,2),(19,5),
(20,5),(20,6),
(21,2),(21,4),
(22,4),(22,5),
(23,3),(23,6),
(24,2),(24,5),
(25,4),(25,6),
(26,2),(26,3),(26,5),
(27,6),(27,2),
(28,3),(28,4),
(29,5),(29,6),
(30,2),(30,4);

-- 更新文章点赞数（与article_like表一致）
UPDATE article SET like_count = (SELECT COUNT(*) FROM article_like WHERE article_like.article_id = article.id);

-- 插入收藏
INSERT INTO article_favorite (article_id, user_id) VALUES
(3,2),(3,5),(3,6),
(1,4),(1,5),
(8,2),(8,4),
(10,3),(10,5),
(14,4),(14,6),
(17,2),(17,5),
(22,4),(22,6),
(25,3),(25,5),
(30,2),(30,4);

-- 插入评论
INSERT INTO comment (article_id, user_id, content, parent_id, reply_to_user_id, like_count) VALUES
(1,3,'写得很详细，跟着做成功了！',0,NULL,5),
(1,4,'MP确实比MyBatis方便太多',0,NULL,3),
(3,2,'RAG现在是落地最多的方案，收藏了',0,NULL,8),
(3,5,'我们项目就是用Milvus做的向量检索',0,NULL,6),
(5,4,'Explain分析慢查询很实用',0,NULL,4),
(8,2,'缓存穿透这个点经常被问到',0,NULL,7),
(10,6,'Agent是未来趋势，顶',0,NULL,9),
(10,3,'Dify用过，确实低代码',10,6,3),
(17,5,'Prompt技巧总结得很好',0,NULL,5),
(22,4,'LoRA训了个医疗模型，效果不错',0,NULL,6),
(25,2,'我们公司也在用Dify',25,4,2),
(30,6,'AI应用架构总结到位',0,NULL,4);
