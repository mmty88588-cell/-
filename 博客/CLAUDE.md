# 个人博客项目 - Claude Code 上下文

## 项目概览
Spring Boot 3.4.7 + MyBatis 3.0.4 + MySQL 8.0 + Redis 个人博客后端，端口 8080。

## 运行环境
- JDK 25 (Eclipse Adoptium): `C:\Users\86180\AppData\Local\Programs\Eclipse Adoptium\jdk-25.0.2.10-hotspot`
- Maven 3.9.6: `C:\tools\apache-maven-3.9.6`
- MySQL 8.0: root / 11230426
- Redis: 127.0.0.1:6379 (无密码)

## 启动命令
```powershell
$env:JAVA_HOME = "C:\Users\86180\AppData\Local\Programs\Eclipse Adoptium\jdk-25.0.2.10-hotspot"
$env:PATH = "C:\tools\apache-maven-3.9.6\bin;$env:PATH"
mvn spring-boot:run
```

或 IDEA 直接运行 `BlogApplication.java`。

## 项目分层
```
controller/   - REST 接口 (Auth, Article, Category, Tag, Comment, Admin)
service/      - 业务逻辑 + Redis 缓存 (impl/)
mapper/       - MyBatis 接口
entity/       - 实体类 (User, Article, Category, Tag, Comment)
dto/          - 请求对象 (LoginRequest, RegisterRequest)
config/       - 配置 + 拦截器 (RedisConfig, WebConfig, LoginInterceptor, AdminInterceptor)
common/       - 通用类 (Result, PageResult)
```

## Redis 缓存设计
| Key | 类型 | TTL | 说明 |
|---|---|---|---|
| `session:{token}` | String | 30min | 用户登录，存 User JSON |
| `article:{id}` | String | 10min | 单篇文章 |
| `article_list:{params}` | String | 60s | 列表查询结果 |
| `hot_articles` | ZSet | 永久 | 浏览量排序，score=浏览数 |

定时任务 `syncViewCountToDb()` 每5分钟将 ZSet 的浏览量同步到 MySQL。

## 权限机制
- `LoginInterceptor`: 拦截 `/api/**`，从 Header `Authorization: Bearer <token>` 取 token，查 Redis 获取 User
- `AdminInterceptor`: 拦截 `/api/admin/**`，校验 role=ADMIN
- 公开路径: `/api/auth/**`, `/api/articles/list`, `/api/articles/hot`, `/api/articles/*/detail`, `/api/categories`, `/api/tags`, `/api/comments/article/*`

## 数据库表 (MySQL blog库)
user, category, tag, article, article_tag, comment
执行 `src/main/resources/schema.sql` 可重建。

## 已知问题 / 注意事项
1. JDK 25 的 `sun.misc.Unsafe` 警告可忽略（来自 Maven/Netty/Guava），不影响功能
2. `activateDefaultTyping` 在 RedisConfig 中配置了类型信息，确保 User 对象从 Redis 反序列化正确
3. `jackson-datatype-jsr310` 已添加，支持 LocalDateTime 序列化
4. 管理员: admin / admin123（BCrypt hash: $2a$10$YvwNCSLAvmOxyvDwfqteRe.up2lPuL8hnfVKY04ouw.bi9nwoq0D6）
5. PageHelper 版本 2.1.0，MyBatis Starter 3.0.4，均兼容 Spring Boot 3.x
6. 已从 Spring Boot 2.7 → 3.4.7 升级，javax.* → jakarta.* 已全部迁移

## 测试账号
| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | admin | admin123 |
| 普通用户 | testuser | 123456 |

## 常用 Maven 命令
```
mvn compile        # 编译
mvn spring-boot:run  # 启动
```

## 下次改进方向（用户可能提出）
- 前端页面（当前只有后端 API）
- 文章标签功能补全（数据库已设计，API 待完善）
- 图片上传
- 分页优化
- 搜索增强
