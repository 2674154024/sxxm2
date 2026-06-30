# 长沙大学生兼职平台 — 启动说明报告

> **日期**: 2026-06-30 | **版本**: V1.0

---

## 一、系统架构概览

```
┌─────────────────────────────────────────────────┐
│  浏览器                                          │
│  http://localhost:5173  (PC管理后台)              │
│  http://localhost:5174  (H5移动端)               │
└────────────┬────────────────────────────────────┘
             │ /api/* 代理
┌────────────▼────────────────────────────────────┐
│  Gateway :8080  (Spring Cloud Gateway)           │
│  路由: /v1/auth/* → user-service                │
│       /v1/job/*  → job-service                  │
│       /v1/public/* → job-service                │
└───┬──────────────┬──────────────┬───────────────┘
    │              │              │
┌───▼──────┐ ┌─────▼─────┐ ┌─────▼──────┐
│user-svc  │ │job-svc    │ │ 其他8个    │
│:8081     │ │:8082      │ │ 微服务     │
└───┬──────┘ └─────┬─────┘ └─────┬──────┘
    │              │              │
┌───▼──────────────▼──────────────▼───────────────┐
│  基础设施 (Docker)                               │
│  MySQL:3307  Redis:6379  ES:9200  RabbitMQ:5672 │
└─────────────────────────────────────────────────┘
```

### 核心服务清单

| 层 | 服务 | 端口 | 说明 |
|----|------|------|------|
| 基础设施 | MySQL 8.0 | 3307 | 数据库（避开本机3306） |
| 基础设施 | Redis 7.0 | 6379 | 缓存 + 限流 |
| 基础设施 | Elasticsearch 7.17 | 9200 | 岗位搜索 |
| 基础设施 | RabbitMQ 3.12 | 5672 | 消息队列 |
| 后端 | Gateway | 8080 | API网关 + JWT鉴权 |
| 后端 | user-service | 8081 | 用户/认证/简历 |
| 后端 | job-service | 8082 | 岗位发布/搜索 |
| 前端 | PC Web | 5173 | 管理后台(Vue3+Element Plus) |
| 前端 | H5 Web | 5174 | 移动端(Vue3+Vant) |

> **说明**: MVP阶段启动3个核心后端服务（共11个微服务）。其他8个服务（match、im、task、payment、complaint、notification、admin、data）按需单独启动。

---

## 二、前置要求

| 工具 | 版本要求 | 检查命令 |
|------|---------|---------|
| Docker Desktop | 24+ | `docker --version` |
| Java JDK | 17+ | `java -version` |
| Maven | 3.9+ | `mvn --version` |
| Node.js | 18+ | `node --version` |
| npm | 9+ | `npm --version` |
| Git Bash | 最新 | `bash --version` |

**端口检查**: 启动前确保以下端口未被占用：
`3307` `6379` `9200` `5672` `8080` `8081` `8082` `5173` `5174`

---

## 三、快速启动（推荐）

### 一键启动全部服务

```bash
# 在项目根目录执行
bash start-all.sh
```

脚本自动完成：
1. ✅ 检查运行环境（Docker/Java/Maven/Node）
2. ✅ 启动基础设施（MySQL/Redis/ES/RabbitMQ）
3. ✅ 启动后端服务（Gateway + user-service + job-service）
4. ✅ 启动前端服务（PC Web + H5 Web）
5. ✅ 冒烟测试API
6. ✅ 显示访问地址

### 其他命令

```bash
bash start-all.sh status    # 查看所有服务状态
bash start-all.sh stop      # 停止所有服务
bash start-all.sh restart   # 重启所有服务
```

---

## 四、分步启动

### 第1步：启动基础设施

```bash
# 在项目根目录执行
docker compose up -d mysql redis elasticsearch rabbitmq

# 等待 MySQL 就绪（约30秒）
docker ps --filter name=parttime-mysql --format '{{.Status}}'
```

### 第2步：构建后端（仅首次或代码变更后）

```bash
cd backend
mvn clean package -DskipTests
```

构建产物在 `backend/*/target/*.jar`，预期大小 50-100MB（fat JAR）。

### 第3步：启动后端服务

```bash
cd backend

# 设置环境变量（从 .env 文件读取）
export MYSQL_ROOT_PASSWORD='parttime@2024'
export REDIS_PASSWORD='parttime@2024'
export RABBITMQ_USER='admin'
export RABBITMQ_PASSWORD='parttime@2024'
export JWT_SECRET='parttime-platform-jwt-secret-2024'
export AES_SECRET_KEY='cGFydHRpbWUyMDI0cGFydHRpbWUyMDI0cGFydHRpbWU='

# 启动 user-service (8081)
nohup java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar \
    --spring.datasource.password="${MYSQL_ROOT_PASSWORD}" \
    --spring.redis.password="${REDIS_PASSWORD}" \
    --spring.rabbitmq.password="${RABBITMQ_PASSWORD}" \
    > /d/sxxm2/logs/user-service.log 2>&1 &

# 启动 job-service (8082)
nohup java -jar job-service/target/job-service-1.0.0-SNAPSHOT.jar \
    --spring.datasource.password="${MYSQL_ROOT_PASSWORD}" \
    --spring.redis.password="${REDIS_PASSWORD}" \
    > /d/sxxm2/logs/job-service.log 2>&1 &

# 启动 gateway (8080)
nohup java -jar gateway/target/gateway-1.0.0-SNAPSHOT.jar \
    --spring.redis.password="${REDIS_PASSWORD}" \
    > /d/sxxm2/logs/gateway.log 2>&1 &

# 等待启动完成（约20秒）
sleep 20
```

### 第4步：启动前端服务

```bash
# PC Web 管理后台
cd pc-web
npm install     # 仅首次
npx vite --port 5173 &

# H5 移动端
cd h5-web
npm install     # 仅首次
npx vite --port 5174 &
```

---

## 五、验证服务

### API冒烟测试

```bash
# 通过网关发送短信验证码
curl -X POST http://localhost:8080/v1/auth/sms-code \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800000001"}'

# 预期返回: {"code":200,"message":"成功","data":"验证码已发送",...}

# 查岗岗位列表
curl "http://localhost:8080/v1/public/job/list?page=1&size=5"

# 预期返回: {"code":200,"data":{"list":[],"total":0,...}}
```

### 浏览器访问

| 页面 | 地址 | 说明 |
|------|------|------|
| **PC管理后台** | http://localhost:5173 | 企业运营端 + 平台管理后台 |
| **H5移动端** | http://localhost:5174 | 学生拓展端 |
| RabbitMQ管理 | http://localhost:15672 | 账号 admin / parttime@2024 |
| Elasticsearch | http://localhost:9200 | 集群状态 |

---

## 六、日志

所有日志位于 `logs/` 目录：

| 文件 | 内容 |
|------|------|
| `logs/gateway.log` | 网关日志（路由、鉴权、限流） |
| `logs/user-service.log` | 用户服务日志（登录、注册、短信） |
| `logs/job-service.log` | 岗位服务日志（发布、搜索、ES同步） |
| `logs/pc-web.log` | PC前端日志 |
| `logs/h5-web.log` | H5前端日志 |

查看实时日志：
```bash
tail -f logs/gateway.log
tail -f logs/user-service.log
```

---

## 七、常见问题

### MySQL 端口 3306 冲突

本机已有 MySQL 占用 3306。项目已将 Docker MySQL 映射到 **3307**。

```bash
# 检查端口占用
netstat -ano | grep ":3306 "
netstat -ano | grep ":3307 "
```

### Gateway 启动报 NoClassDefFoundError

可能是 `AuditLogAspect` 依赖 Servlet API 但 Gateway 是 Netty 环境。已通过 `@ConditionalOnClass` 修复。确认代码已更新后重新构建：

```bash
cd backend && mvn clean package -DskipTests
```

### 端口被占用

```bash
# Windows 查看端口占用
netstat -ano | findstr "8080"

# 强制结束进程
taskkill //F //PID <PID>
```

### 前端代理不生效

检查 `vite.config.ts` 中 proxy 配置：
```ts
proxy: {
  '/api': {
    target: 'http://localhost:8080',  // 指向 Gateway
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, ''),
  },
}
```

### 查看特定服务是否运行

```bash
bash start-all.sh status
```

---

## 八、停止服务

```bash
# 一键停止
bash start-all.sh stop

# 或分别停止
taskkill //F //IM java.exe    # 停止后端
taskkill //F //IM node.exe    # 停止前端
docker compose stop           # 停止基础设施
```

---

## 九、开发环境 vs 生产环境

| 项目 | 开发环境（本机） | 生产环境（Docker部署） |
|------|-----------------|----------------------|
| 启动方式 | `bash start-all.sh` | `docker compose up -d` |
| 后端部署 | 直接 java -jar | Docker 容器 |
| 前端部署 | Vite dev server | Nginx 静态文件 |
| 网关 | 8080 | 80/443 (Nginx → Gateway) |
| 数据库 | Docker MySQL | Docker MySQL |
| CI/CD | 无 | Jenkins pipeline |

生产部署参见 `Jenkinsfile` 和 `docker-compose.yml`。

---

## 十、启动流程速查

```
┌──────────────────────────────────────────────────┐
│             一键启动命令                           │
│         $ bash start-all.sh                      │
└──────────────┬───────────────────────────────────┘
               │
     ┌─────────▼──────────┐
     │ 1. 检查环境         │  docker/java/node 可用?
     └─────────┬──────────┘
               │ ✓
     ┌─────────▼──────────┐
     │ 2. 启动基础设施      │  MySQL Redis ES RabbitMQ
     └─────────┬──────────┘
               │ ✓
     ┌─────────▼──────────┐
     │ 3. 启动后端服务      │  gateway user-svc job-svc
     └─────────┬──────────┘
               │ ✓
     ┌─────────▼──────────┐
     │ 4. 启动前端服务      │  PC Web H5 Web
     └─────────┬──────────┘
               │ ✓
     ┌─────────▼──────────┐
     │ 5. 冒烟测试         │  API + 页面可达性
     └─────────┬──────────┘
               │ ✓
     ┌─────────▼──────────┐
     │ 浏览器访问           │
     │ http://localhost:5173 │
     │ http://localhost:5174 │
     └──────────────────────┘
```
