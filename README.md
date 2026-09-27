# GPU Manager

GPU Manager 是一个面向实验室 GPU 服务器资源管理与预约场景的前后端分离项目。

项目提供用户注册登录、JWT 身份认证、角色权限控制、GPU 服务器管理、GPU 预约、预约冲突检测、Redis 缓存、分页查询等功能。

## 技术栈

### 后端

- Java 21
- Spring Boot 4
- MyBatis
- MySQL 8
- Redis
- JWT
- BCrypt
- Spring Validation
- Swagger / OpenAPI

### 前端

- Vue 3
- Vite
- Element Plus
- Axios
- Vue Router

## 项目结构

```text
gpu-manager
├── gpu-manager
│   ├── src/main/java
│   ├── src/main/resources
│   └── pom.xml
│
├── gpu-manager-web
│   ├── src
│   │   ├── api
│   │   ├── components
│   │   ├── router
│   │   ├── utils
│   │   └── views
│   └── package.json
│
└── README.md
```

## 核心功能

### 用户模块

- 用户注册
- 用户登录
- BCrypt 密码加密
- JWT 身份认证
- 获取当前用户信息

### 权限管理

系统包含两种角色：

- USER
- ADMIN

管理员可以：

- 新增 GPU 服务器
- 修改 GPU 服务器
- 删除 GPU 服务器
- 查看全部预约记录

普通用户可以：

- 查看 GPU 服务器
- 创建预约
- 查看自己的预约
- 取消自己的预约

后端通过自定义 `@AdminOnly` 注解配合 JWT 拦截器进行权限校验。

## GPU 服务器管理

支持：

- 查询服务器列表
- 查询服务器详情
- 新增服务器
- 修改服务器
- 删除服务器

服务器状态包括：

```
AVAILABLE
BUSY
```

## GPU 预约管理

用户可以选择 GPU 服务器并指定：

- 开始时间
- 结束时间

系统会检查：

- 开始时间不能早于当前时间
- 结束时间必须晚于开始时间
- 同一服务器预约时间不能冲突

时间冲突判断逻辑：

```
start_time < newEndTime
AND
end_time > newStartTime
```

相邻时间段允许预约，例如：

```
10:00 - 12:00
12:00 - 14:00
```

不会被判定为冲突。

## 并发控制

创建预约时使用数据库悲观锁：

```
SELECT id
FROM gpu_server
WHERE id = ?
FOR UPDATE
```

结合 Spring `@Transactional` 保证同一 GPU 服务器的预约检查和创建过程串行执行，降低并发情况下重复预约的风险。

## Redis 缓存

GPU 服务器列表和详情使用 Redis 缓存。

### 列表缓存

```
gpu:servers:list
```

### 详情缓存

```
gpu:server:{id}
```

针对常见缓存问题进行了基础处理：

- 缓存穿透：不存在的数据缓存 `NULL`
- 缓存雪崩：缓存过期时间加入随机值
- 数据修改后主动删除对应缓存

## JWT 认证

登录成功后后端返回 JWT。

前端将 JWT 保存到 `localStorage`。

Axios 请求拦截器自动添加：

```
Authorization: Bearer <token>
```

后端 `JwtInterceptor` 对 Token 进行统一验证。

## 前端页面

目前包含：

- 登录页
- 注册页
- GPU 服务器列表
- GPU 预约
- 我的预约
- 管理员预约管理

前端根据用户角色显示不同操作按钮，同时后端进行最终权限校验。

## 本地运行

### 1. 准备数据库

创建 MySQL 数据库：

```
CREATE DATABASE gpu_manager
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 2. 后端配置

复制：

```
application-dev.example.properties
```

为：

```
application-dev.properties
```

修改数据库密码和 JWT Secret。

示例：

```
spring.datasource.url=jdbc:mysql://localhost:3306/gpu_manager?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.data.redis.host=localhost
spring.data.redis.port=6379

jwt.secret=YOUR_JWT_SECRET
jwt.expire-time=86400000
```

注意：

`application-dev.properties` 已加入 `.gitignore`，不要提交真实密码和密钥。

### 3. 启动后端

进入：

```
gpu-manager
```

Windows：

```
.\mvnw.cmd spring-boot:run
```

默认运行：

```
http://localhost:8080
```

### 4. 启动前端

进入：

```
gpu-manager-web
```

安装依赖：

```
npm install
```

启动：

```
npm run dev
```

默认访问：

```
http://localhost:5173
```

## API 文档

启动后端后，可以通过 Swagger UI 查看接口文档。

```
http://localhost:8080/swagger-ui/index.html
```
