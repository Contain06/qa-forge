# QAForge — API 自动化测试平台

从零构建的接口自动化测试工具，支持 HTTP 请求编排、多维度断言校验、响应数据提取与变量插值替换、测试计划批量执行与结果回溯。

## 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 3.5 |
| 安全 | Spring Security 6 + JWT (jjwt 0.11.5, HS256) |
| ORM | MyBatis-Plus 3.5 |
| 数据库 | MySQL 8.x |
| 缓存 | Redis（Token 黑名单） |
| HTTP 客户端 | OkHttp 4.12 |
| JSON 处理 | Jackson + Jayway JsonPath |
| API 文档 | Knife4j 5.0 (Swagger) |

## 快速启动

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE qa_forge CHARACTER SET utf8mb4;"

# 2. 修改 application.yml 中的数据库连接和 Redis 连接

# 3. 启动
./mvnw spring-boot:run
```

后端运行在 `http://localhost:8080`，Swagger 文档在 `http://localhost:8080/doc.html`。

## 项目结构

```
src/main/java/com/atcontain/qaforge/
├── builder/          # 请求构建器（三层合并 + 变量替换）
├── config/           # MyBatis-Plus 配置
├── controller/       # REST 控制器（11 个模块）
├── dto/              # 数据传输对象
├── entity/           # 数据库实体（12 张表）
├── exception/        # 全局异常处理
├── executor/         # 执行引擎（HTTP、断言、提取）
├── mapper/           # MyBatis-Plus Mapper
├── security/         # Spring Security + JWT
│   ├── config/       # SecurityConfig
│   ├── filter/       # JwtAuthenticationFilter
│   └── util/         # JwtTokenUtil、SecurityUtils
├── service/          # 业务服务接口
│   └── impl/         # 服务实现
├── util/             # 工具类（JSON 处理、Redis 黑名单）
└── vo/               # 视图对象
```

## 核心设计

### 请求执行链路

```
Environment (baseUrl) + ApiInfo (模板) + ApiCase (覆盖值)
        ↓
HttpRequestBuilder.build()  →  三层合并 + {{key}} 变量替换
        ↓
HttpExecutor.execute()      →  OkHttp 发送请求
        ↓
AssertionExecutor.execute() →  STATUS_CODE / JSON_PATH / BODY
        ↓
ExtractExecutor.execute()   →  JSONPath / 正则 提取响应数据
        ↓
env_variable 自动回写       →  后续请求直接用 {{key}} 引用
```

### 无状态认证

```
请求 → JwtAuthenticationFilter → 解析 JWT → SecurityContext
                                              ↓
                              SecurityUtils.getCurrentUserId()
```

- JWT 承载用户身份，24h 过期，HS256 签名
- 用户 ID 存储在 `authentication.setDetails(userId)`
- Controller 通过 `SecurityUtils.getCurrentUserId()` 获取
- 登出时 jti 加入 Redis 黑名单，TTL 自动过期

### 所有权多租户

所有资源访问向上溯源到 Project.ownerId，用户只能操作自己的项目。返回 404 而非 403，隐藏资源存在性。

## API 端点

| 模块 | 路径 |
|------|------|
| 用户认证 | `/user/login`、`/user/register`、`/user/logout` |
| 项目管理 | `/project/*` |
| 环境管理 | `/environment/*` |
| 环境变量 | `/env-variable/*` |
| 接口模板 | `/api-info/*` |
| 测试用例 | `/api-case/*`（含批量导入、单用例执行） |
| 断言规则 | `/api-assertion/*` |
| 提取规则 | `/api-extract/*` |
| 测试计划 | `/test-plan/*`（含绑定用例、计划执行） |
| 执行记录 | `/run-record/*` |
