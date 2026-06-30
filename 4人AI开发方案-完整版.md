d# 长沙大学生兼职平台 — 4人AI开发完整方案

> **版本**: V1.0 | **日期**: 2026-06-29 | **MVP周期**: 16周
>
> 本文档为4人+4个AI代理协作开发方案。每人配备一个AI代理，按照本文档的角色划分、提示词模板、依赖版本清单独立工作。

---

## 目录

1. [总体原则](#一总体原则)
2. [依赖版本清单（已锁定，禁止修改）](#二依赖版本清单已锁定禁止修改)
3. [项目初始化脚本](#三项目初始化脚本)
4. [Agent-1: 后端开发工程师](#四agent-1-后端开发工程师)
5. [Agent-2: 前端开发工程师](#五agent-2-前端开发工程师)
6. [Agent-3: 测试+运维工程师](#六agent-3-测试运维工程师)
7. [Agent-4: 产品经理+UI设计师](#七agent-4-产品经理ui设计师)
8. [16周排期表](#八16周排期表)
9. [AI使用约定](#九ai使用约定)

---

## 一、总体原则

### 1.1 AI开发核心规则

```
1. 每个AI代理只读取与自己角色相关的章节，通过「接口契约」和「数据库Schema」与其他角色解耦
2. 所有依赖版本已锁定在本文档第二章，禁止AI自行升级或降级
3. AI每完成一个Phase，必须输出对应产物（代码/文档/配置）并标注Phase完成状态
4. 前后端通过统一的API接口契约通信（见总纲第五章），接口变更必须同步更新契约文档
5. 所有代码生成后必须包含：单元测试、注释说明、错误处理
6. 涉及金额的计算统一使用BigDecimal，禁止float/double
7. 敏感字段（身份证/手机号/银行卡）必须AES-256加密存储，API返回脱敏
8. 每个接口必须添加@AuditLog注解用于操作审计
```

### 1.2 AI代理配置要求

每个AI代理需要具备以下能力：
- 读取并理解指定目录的代码
- 按照提示词生成符合规范的代码
- 支持上下文记忆（同一角色的连续开发）
- 能执行编译/构建命令验证代码正确性

---

## 二、依赖版本清单（已锁定，禁止修改）

### 2.1 后端依赖 (Maven pom.xml)

```xml
<!-- ========== 父工程属性 ========== -->
<properties>
    <java.version>1.8</java.version>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

    <!-- Spring 生态 -->
    <spring-boot.version>2.7.18</spring-boot.version>
    <spring-cloud.version>2021.0.9</spring-cloud.version>
    <spring-cloud-alibaba.version>2021.0.6.1</spring-cloud-alibaba.version>

    <!-- 数据库 -->
    <mysql-connector.version>8.0.33</mysql-connector.version>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
    <druid.version>1.2.20</druid.version>

    <!-- 缓存 & 搜索 -->
    <redisson.version>3.24.3</redisson.version>
    <elasticsearch.version>7.17.20</elasticsearch.version>

    <!-- 消息队列 -->
    <rabbitmq.version>3.12.0</rabbitmq.version>

    <!-- 工具类 -->
    <hutool.version>5.8.25</hutool.version>
    <fastjson2.version>2.0.43</fastjson2.version>
    <jjwt.version>0.9.1</jjwt.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>

    <!-- 第三方SDK -->
    <wechat-pay.version>0.4.12</wechat-pay.version>
    <alipay-sdk.version>4.38.157.ALL</alipay-sdk.version>
    <tencentcloud-sdk.version>3.1.1012</tencentcloud-sdk.version>
    <aliyun-sms.version>2.0.23</aliyun-sms.version>
    <aliyun-oss.version>3.17.4</aliyun-oss.version>

    <!-- 测试 -->
    <junit.version>4.13.2</junit.version>
    <mockito.version>4.11.0</mockito.version>
</properties>

<!-- ========== 核心依赖清单 ========== -->
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>2.7.18</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
        <version>2.7.18</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
        <version>2.7.18</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
        <version>2.7.18</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
        <version>2.7.18</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
        <version>2.7.18</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
        <version>2.7.18</version>
    </dependency>

    <!-- Spring Cloud Gateway -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
        <version>3.1.9</version>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- Druid连接池 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.2.20</version>
    </dependency>

    <!-- Redisson (Redis分布式锁) -->
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson-spring-boot-starter</artifactId>
        <version>3.24.3</version>
    </dependency>

    <!-- Elasticsearch -->
    <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-high-level-client</artifactId>
        <version>7.17.20</version>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.9.1</version>
    </dependency>

    <!-- Hutool工具类 -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.8.25</version>
    </dependency>

    <!-- FastJSON2 -->
    <dependency>
        <groupId>com.alibaba.fastjson2</groupId>
        <artifactId>fastjson2</artifactId>
        <version>2.0.43</version>
    </dependency>

    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>1.5.5.Final</version>
        <scope>provided</scope>
    </dependency>

    <!-- 微信支付SDK -->
    <dependency>
        <groupId>com.github.wechatpay-apiv3</groupId>
        <artifactId>wechatpay-java</artifactId>
        <version>0.4.12</version>
    </dependency>

    <!-- 支付宝SDK -->
    <dependency>
        <groupId>com.alipay.sdk</groupId>
        <artifactId>alipay-sdk-java</artifactId>
        <version>4.38.157.ALL</version>
    </dependency>

    <!-- 腾讯云SDK (IM/TRTC/实名认证) -->
    <dependency>
        <groupId>com.tencentcloudapi</groupId>
        <artifactId>tencentcloud-sdk-java</artifactId>
        <version>3.1.1012</version>
    </dependency>

    <!-- 阿里云OSS -->
    <dependency>
        <groupId>com.aliyun.oss</groupId>
        <artifactId>aliyun-sdk-oss</artifactId>
        <version>3.17.4</version>
    </dependency>

    <!-- 阿里云短信 -->
    <dependency>
        <groupId>com.aliyun</groupId>
        <artifactId>dysmsapi20170525</artifactId>
        <version>2.0.23</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>

    <!-- 测试 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <version>2.7.18</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>4.11.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2.2 前端依赖版本

#### 2.2.1 小程序端 (uni-app + Vue2)

```json
{
  "dependencies": {
    "uni-app": "2.0.2-4020420240722002",
    "vue": "2.6.14",
    "vuex": "3.6.2",
    "@dcloudio/uni-ui": "1.5.6",
    "@dcloudio/uni-h5": "2.0.2-4020420240722002",
    "@dcloudio/uni-mp-weixin": "2.0.2-4020420240722002",
    "@dcloudio/uni-mp-alipay": "2.0.2-4020420240722002",
    "vant-weapp": "1.11.5",
    "axios": "0.27.2",
    "dayjs": "1.11.10",
    "crypto-js": "4.2.0"
  },
  "devDependencies": {
    "@dcloudio/types": "3.3.3",
    "sass": "1.69.5",
    "sass-loader": "10.4.1",
    "postcss": "8.4.32",
    "autoprefixer": "10.4.16"
  }
}
```

#### 2.2.2 PC端 (Vue3 + Element Plus)

```json
{
  "dependencies": {
    "vue": "3.3.13",
    "vue-router": "4.2.5",
    "pinia": "2.1.7",
    "element-plus": "2.4.4",
    "@element-plus/icons-vue": "2.3.1",
    "echarts": "5.4.3",
    "vue-echarts": "6.6.8",
    "axios": "1.6.3",
    "dayjs": "1.11.10",
    "crypto-js": "4.2.0",
    "xlsx": "0.18.5",
    "file-saver": "2.0.5",
    "vue-draggable-plus": "0.3.5",
    "@vueuse/core": "10.7.1"
  },
  "devDependencies": {
    "typescript": "5.2.2",
    "@vitejs/plugin-vue": "4.5.2",
    "vite": "5.0.10",
    "sass": "1.69.5",
    "unplugin-auto-import": "0.17.3",
    "unplugin-vue-components": "0.26.0",
    "eslint": "8.55.0",
    "eslint-plugin-vue": "9.19.2",
    "prettier": "3.1.0"
  }
}
```

#### 2.2.3 H5端 (Vue3 + Vant)

```json
{
  "dependencies": {
    "vue": "3.3.13",
    "vue-router": "4.2.5",
    "pinia": "2.1.7",
    "vant": "4.8.5",
    "@vant/use": "1.6.0",
    "axios": "1.6.3",
    "dayjs": "1.11.10",
    "crypto-js": "4.2.0"
  },
  "devDependencies": {
    "typescript": "5.2.2",
    "@vitejs/plugin-vue": "4.5.2",
    "vite": "5.0.10",
    "postcss": "8.4.32",
    "postcss-px-to-viewport-8-plugin": "1.2.5",
    "autoprefixer": "10.4.16",
    "sass": "1.69.5"
  }
}
```

### 2.3 DevOps依赖版本

```yaml
# docker-compose.yml 服务版本
mysql: mysql:8.0.35
redis: redis:7.0.12-alpine
elasticsearch: elasticsearch:7.17.20
rabbitmq: rabbitmq:3.12.10-management-alpine
nginx: nginx:1.25.3-alpine
jenkins: jenkins/jenkins:2.440.1-lts-jdk11

# 工具版本
docker: 24.0.x
docker-compose: 2.23.x
node: 18.18.0 (LTS)
npm: 9.8.1
java: 1.8.0_392 (Amazon Corretto 8)
maven: 3.9.6
```

---

## 三、项目初始化脚本

### 3.1 后端项目初始化（Agent-1使用）

```bash
# AI代理执行以下命令创建Spring Boot父工程
mvn archetype:generate \
  -DgroupId=com.parttime \
  -DartifactId=part-time-platform \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

cd part-time-platform

# 创建微服务模块
mvn archetype:generate -DgroupId=com.parttime -DartifactId=gateway -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=common -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=user-service -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=job-service -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=match-service -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=im-service -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=task-service -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=payment-service -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=complaint-service -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=notification-service -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=data-service -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.parttime -DartifactId=admin-service -DinteractiveMode=false
```

### 3.2 前端项目初始化（Agent-2使用）

```bash
# 小程序端 - 使用HBuilderX创建uni-app项目（选Vue2版本）
# 或命令行：
npx @dcloudio/uvm@latest
npx degit dcloudio/uni-preset-vue#v2 miniapp

# PC端 - Vite + Vue3
npm create vite@5.0.10 pc-web -- --template vue-ts
cd pc-web && npm install

# H5端 - Vite + Vue3
npm create vite@5.0.10 h5-web -- --template vue-ts
cd h5-web && npm install
```

---

## 四、Agent-1: 后端开发工程师

### 4.1 角色配置

```
角色名称: AI后端开发工程师
工作目录: backend/
依赖版本: 见第二章2.1节
参考文档:
  - AI驱动开发总纲.md (第三章目录结构、第四章数据库Schema、第五章API契约、第六章RBAC矩阵、第七章业务规则)
  - 模块开发设计框架/API开发解决方案.md
  - 模块开发设计框架/安全加密方案.md
  - 模块开发设计框架/数据库框架设计.md
```

### 4.2 开发提示词模板

#### Phase 1: 基础架构搭建（第1-2周）

**提示词1.1 — 创建父工程与common模块：**

```
你是Spring Boot后端开发专家。请按以下要求创建项目：

【技术栈】
- Java 8, Spring Boot 2.7.18, Maven 3.9.6
- 完整依赖见 pom.xml（已提供）

【任务】
1. 创建Maven父工程 part-time-platform，packaging=pom
2. 创建common模块，包含以下工具类：

【Common模块内容】
A. AES256加密工具类 (AesUtil.java)
   - 从环境变量 AES_SECRET_KEY 读取密钥（Base64编码的32字节密钥）
   - encrypt(String plainText): String — 加密并返回Base64密文
   - decrypt(String cipherText): String — 解密返回明文
   - 使用AES/CBC/PKCS5Padding模式，IV随机生成并拼接在密文前16字节

B. 统一响应格式 (R.java)
   - code: Integer（200/400/401/403/409/422/500）
   - message: String
   - data: T（泛型）
   - trace_id: String（UUID）
   - 静态工厂方法: R.ok(data), R.fail(code, message)

C. 全局异常处理器 (GlobalExceptionHandler.java)
   - @RestControllerAdvice
   - 处理: MethodArgumentNotValidException → 400
   - 处理: BusinessException（自定义）→ 对应业务错误码
   - 处理: Exception → 500，但不暴露堆栈给前端

D. JWT工具类 (JwtUtil.java)
   - 从环境变量 JWT_SECRET 读取签名密钥
   - generateToken(userId, role, permissions, expireSeconds=604800): String
   - parseToken(String token): Claims
   - validateToken(String token): boolean
   - JWT payload包含: user_id, role, permissions[], exp

E. DFA敏感词过滤器 (SensitiveWordFilter.java)
   - 从敏感词库文件加载词库（押金、培训费、中介费、高薪零门槛、垫资、刷单、先交钱）
   - containsSensitiveWord(String text): boolean
   - getSensitiveWords(String text): List<String>
   - 使用DFA算法，支持最小匹配原则

F. @AuditLog注解 + AOP切面
   - 自定义注解 @AuditLog(module, action)
   - AOP切面拦截所有标注@AuditLog的方法
   - 自动记录: operator_id（从JWT获取）, module, action, request_params（脱敏）, ip_address, client_type, trace_id
   - 存储到 t_audit_log 表（异步写入）

G. BigDecimal工具类 (AmountUtil.java)
   - add, subtract, multiply, divide（全部使用BigDecimal，禁止float/double）
   - 个税计算: calcTax(BigDecimal cumulativeSalary) — 累计预扣法
   - 长沙最低时薪校验: MIN_HOURLY_WAGE = 17

【输出要求】
- 每个类包含完整的JavaDoc注释
- 每个public方法包含单元测试
- 代码遵循阿里巴巴Java开发手册规范
- 所有金额计算使用BigDecimal
```

**提示词1.2 — 创建Gateway网关模块：**

```
你是Spring Cloud Gateway专家。请创建gateway模块：

【任务】
创建 gateway/ 模块，包含：

A. GatewayApplication.java — 启动类
B. application.yml 配置：
   - 路由规则（根据路径前缀转发到对应微服务）
   - CORS跨域配置（允许 miniapp/h5/pc-enterprise/pc-admin 来源）
   - 限流配置（Redis令牌桶算法）
C. JwtAuthFilter.java — 全局过滤器：
   - 从Authorization头提取Bearer Token
   - 调用JwtUtil验证令牌
   - 将user_id, role, permissions写入请求头传递给下游
   - 公开路径白名单: /v1/auth/**, /v1/public/**
D. RateLimitConfig.java — 限流配置：
   - 公开接口: 单IP 10次/分钟
   - 核心业务: 单用户 5次/分钟
   - 管理后台: 单管理员 20次/分钟
E. CorsConfig.java — CORS配置
F. AuditLogGatewayFilter.java — 网关层审计日志记录

【路由映射】
- /v1/auth/** → user-service (8081)
- /v1/student/** → user-service (8081)
- /v1/enterprise/** → user-service (8081)
- /v1/job/** → job-service (8082)
- /v1/match/** → match-service (8083)
- /v1/im/** → im-service (8084)
- /v1/task/** → task-service (8085)
- /v1/payment/** → payment-service (8086)
- /v1/complaint/** → complaint-service (8087)
- /v1/notification/** → notification-service (8088)
- /v1/pc/** → 按子路径路由到对应服务
```

#### Phase 2: 用户服务（第2-3周）

**提示词1.3 — 创建user-service：**

```
你是Spring Boot微服务开发专家。请创建user-service模块：

【要求】
- 端口 8081
- 集成MyBatis-Plus 3.5.5，Druid连接池
- 包含完整的Controller/Service/Mapper/Entity分层
- 所有接口返回R.java统一格式
- 所有核心接口加@AuditLog注解
- 敏感字段（身份证/手机号）使用AesUtil加密存储
- API返回时自动脱敏（手机号中间4位星号，身份证中间8位星号）

【建表SQL】（先执行以下DDL）
-- 见AI驱动开发总纲.md 第四章完整DDL

【需要实现的接口和业务逻辑】

A. 学生注册登录
   POST /v1/auth/wechat-login
   - 接收微信code，调用微信API获取openid
   - 首次登录创建t_student记录（user_id=UUID, verify_status=0）
   - 返回JWT token + 用户基本信息
   
   POST /v1/auth/phone-login
   - 接收手机号+短信验证码
   - 首次登录创建记录
   - 返回JWT token

   POST /v1/auth/sms-code
   - 发送短信验证码（60秒内不允许重复发送）
   - 验证码存入Redis，有效期5分钟

B. 学生实名认证
   POST /v1/student/auth/register
   - 接收: real_name, student_no, school_id, id_card（加密存储）, phone（加密存储）
   - 调用学信网接口核验（核验通过 → verify_status=2，失败→3）
   - 核验通过后更新用户信息
   - 校验: 未实名学生调用此接口，已实名返回409

C. 企业注册认证
   POST /v1/enterprise/auth/register
   - 接收: enterprise_name, credit_code, business_license（上传至OSS）, legal_person, contact_name, contact_phone（加密）
   - 生成enterprise_id，verify_status=1（审核中）
   - 调用天眼查API核验企业信用
   - 通知审核管理员（通过RabbitMQ发送审核任务）

D. 学生个人信息管理
   GET /v1/student/profile — 查看个人信息（脱敏）
   PUT /v1/student/profile — 修改个人信息
   POST /v1/student/resume — 创建/更新简历（available_time存JSON）
   GET /v1/student/resume — 查看简历

E. 信用评分定时任务
   - 每日凌晨2点执行
   - 学生信用分: 初始100分 + 履约加分（按时打卡+5/次）- 投诉减分（有效投诉-20/次）
   - 企业信用分: 初始100分 + 薪资准时发放+5/次 - 投诉减分（有效投诉-20/次）
   - 分数范围0-200

F. RBAC权限拦截器
   - 从JWT拦截器传来的Header中读取role和permissions
   - 配置URL-角色映射表
   - 无权限返回403

【输出要求】
- 每个Service方法包含完整注释
- 金额使用BigDecimal
- 关键业务流程包含事务注解@Transactional
- 提供Postman Collection JSON文件用于接口测试
```

#### Phase 3: 岗位服务（第3-4周）

**提示词1.4 — 创建job-service：**

```
你是Elasticsearch + Spring Boot开发专家。请创建job-service模块：

【要求】
- 端口 8082
- 使用Elasticsearch 7.17.20 RestHighLevelClient
- 岗位数据双写（MySQL + ES），通过定时任务同步保证最终一致性

【需要实现的接口】

A. 岗位CRUD（企业端）
   POST /v1/enterprise/job/publish — 发布岗位
   - 校验: 企业已认证（verify_status=2），否则返回422
   - 敏感词检测: 调用SensitiveWordFilter.containsSensitiveWord()
     包含敏感词 → 自动标记status=0（待审核），通知审核管理员
     不包含 → 自动标记status=1（已发布）
   - 时薪校验: salary_amount >= 17（低于17返回422，提示"不低于长沙最低时薪"）
   - 存储经纬度（调用腾讯地图API geocoding）
   - 双写MySQL + ES索引

   PUT /v1/enterprise/job/update — 编辑岗位
   PUT /v1/enterprise/job/offline — 下架岗位
   GET /v1/enterprise/job/my-list — 我的岗位列表

B. 岗位搜索（学生端+公开）
   GET /v1/public/job/list — 公开岗位列表（分页）
   GET /v1/student/job/list — LBS岗位列表
   - 参数: longitude, latitude, distance(默认5km), salary_min(默认17), salary_max, settlement_type, industry_tag, keyword, page, size
   - ES查询: geo_distance筛选 + bool query多条件组合
   - 排序: 距离降序 + 发布时间降序 + 信用分降序
   - 响应时间要求 ≤200ms

   GET /v1/student/job/detail?job_id=xxx — 岗位详情
   - 企业联系方式脱敏展示
   - 返回企业信用分、认证标识、是否含意外险

C. 岗位审核（管理后台）
   GET /v1/pc/admin/job/audit/list — 待审核岗位列表
   POST /v1/pc/admin/job/audit — 审核通过/驳回（需填理由）

D. ES索引同步定时任务
   - 每5分钟执行一次增量同步
   - 每天凌晨3点全量重建索引
   - 同步字段: job_id, job_title, job_type, industry_tag, salary_type, salary_amount, settlement_type, work_address, longitude, latitude, work_time, skill_require, recruit_num, status, enterprise_id(关联企业名+信用分)

E. 岗位热度计数
   - 浏览量存入Redis: job:view:{job_id}，每10分钟同步到MySQL
   - 投递量存入Redis: job:apply:{job_id}，实时同步

【ES索引Mapping】
{
  "mappings": {
    "properties": {
      "job_id": {"type": "keyword"},
      "job_title": {"type": "text", "analyzer": "ik_max_word"},
      "job_type": {"type": "keyword"},
      "industry_tag": {"type": "keyword"},
      "salary_type": {"type": "integer"},
      "salary_amount": {"type": "double"},
      "settlement_type": {"type": "integer"},
      "work_address": {"type": "text"},
      "location": {"type": "geo_point"},
      "work_time": {"type": "nested"},
      "skill_require": {"type": "text", "analyzer": "ik_max_word"},
      "status": {"type": "integer"},
      "enterprise_name": {"type": "keyword"},
      "enterprise_credit_score": {"type": "integer"},
      "is_insured": {"type": "integer"},
      "create_time": {"type": "date"}
    }
  }
}
```

#### Phase 4: 匹配服务（第4-5周）

**提示词1.5 — 创建match-service：**

```
你是推荐算法Spring Boot开发专家。请创建match-service模块：

【要求】
- 端口 8083
- 实现智能匹配算法，匹配分数0-100

【匹配算法】
综合评分 = 技能匹配度 × 40% + 时间可用性 × 30% + 通勤距离 × 30%

A. 技能匹配度（40分）
   - 学生skill_tags与岗位skill_require做交集
   - 完全匹配=40分，部分匹配=按比例给分，无匹配=10分

B. 时间可用性（30分）
   - 学生available_time（JSON）与岗位work_time（JSON）求交集
   - 覆盖度≥80%=30分，50-79%=20分，<50%=10分，完全无交集=0分

C. 通勤距离（30分）
   - 计算学生位置与岗位位置直线距离（Haversine公式）
   - ≤1km=30分，1-3km=25分，3-5km=20分，5-10km=10分，>10km=5分

【需要实现的接口】
POST /v1/match/calculate — 计算单个学生×单个岗位的匹配分
POST /v1/match/recommend — 为学生推荐Top20岗位
   - 获取学生简历（技能+时间+位置）
   - 从ES搜索5km内的岗位
   - 对每个岗位计算匹配分
   - 排序取Top20
   - 记录t_job_match_log

POST /v1/enterprise/match-candidates — 为企业岗位推荐候选人
   - 获取岗位要求（技能+时间+位置）
   - 从ES搜索附近学生
   - 计算匹配分，取Top20
```

#### Phase 5: 核心流程服务（第5-8周）

**提示词1.6 — 创建im-service：**

```
你是WebSocket + 腾讯云IM开发专家。请创建im-service模块：

【要求】
- 端口 8084
- 支持WebSocket长连接
- 对接腾讯云IM SDK 3.1.1012

【需要实现的接口】
A. WebSocket连接管理
   - 连接时验证JWT令牌（从URL参数传递）
   - 维护 userId → WebSocketSession 映射（ConcurrentHashMap）
   - 心跳检测: 每30秒ping/pong，超时3次断开
   - 断线重连: 客户端自动重连，保留最近50条未读消息

B. 消息收发
   POST /v1/student/im/send — 发送IM消息
   - 消息体: target_id, content, message_type(text/image/file)
   - 存储到MySQL（不可删除，留存3年）
   - 通过WebSocket实时推送给在线用户
   - 离线用户: 存入Redis未读队列，上线后推送

   GET /v1/student/im/messages — 获取历史消息
   - 参数: target_id, page, size
   - 按时间倒序，支持分页

   GET /v1/student/im/conversation-list — 会话列表
   - 返回所有有消息往来的用户+最后一条消息+未读数

C. 视频面试（对接腾讯云TRTC）
   POST /v1/enterprise/invite-interview — 邀请面试
   - 接收: apply_id, interview_time
   - 更新t_job_apply（apply_status=1待面试, interview_time）
   - 生成TRTC房间号，通知学生

【消息体存储格式】
{
  "message_id": "uuid",
  "from_id": "user_id",
  "to_id": "user_id",
  "content": "消息内容",
  "message_type": "text/image/file",
  "timestamp": 1700000000000,
  "is_read": 0
}
```

**提示词1.7 — 创建task-service：**

```
你是GPS打卡 + 工时计算开发专家。请创建task-service模块：

【要求】
- 端口 8085

【需要实现的接口】
A. GPS打卡
   POST /v1/student/clock — GPS打卡签到/签退
   - 接收: job_id, clock_type(1签到/2签退), longitude, latitude
   - 校验: 学生已录用该岗位（apply_status=2）
   - 校验: 打卡坐标与岗位坐标距离≤500米（Haversine公式）
     超过500米 → 允许打卡但标记is_abnormal=1，需学生申诉说明
   - 签退时自动计算工时（签退时间-签到时间）
   - 同一天同一岗位最多签到一次、签退一次（重复返回409）

   GET /v1/student/clock/records — 打卡记录列表
   - 参数: job_id(可选), date(可选), page, size

B. 工时确认（企业端）
   GET /v1/enterprise/attendance/list — 查看排班+打卡记录
   POST /v1/enterprise/attendance/confirm — 确认工时
   - 接收: student_id, job_id, work_date, confirmed_hours, note
   - 异常打卡需审核: is_abnormal=1 的记录需人工确认
   - 确认后标记 settlement_status=1（待企业确认薪资）

C. 工时自动计算
   - 每天凌晨1点执行
   - 汇总当日所有已完成签退的正常打卡记录
   - 计算工时（精确到0.5小时）
   - 生成待确认工时列表推送给企业

D. 异常申诉
   POST /v1/student/clock/appeal — 打卡异常申诉
   - 接收: record_id, appeal_reason, evidence_urls
   - 通知企业审核
```

**提示词1.8 — 创建payment-service：**

```
你是支付结算Spring Boot开发专家。请创建payment-service模块：

【重要】所有金额使用BigDecimal！支付接口必须实现幂等性！

【要求】
- 端口 8086
- 对接微信支付API v3 和支付宝API

【需要实现的接口】
A. 薪资托管
   POST /v1/enterprise/salary/escrow — 企业预存薪资到托管账户
   - 接收: job_id, total_amount（BigDecimal）
   - 调用微信支付/支付宝统一下单API
   - 生成escrow_id，创建t_salary_escrow记录(status=0待支付)
   - 支付回调成功后更新status=1（已托管）

   GET /v1/enterprise/salary/escrow/balance — 查看托管账户余额
   - 返回: total_amount, paid_amount, freeze_amount, available_amount

B. 薪资计算与发放
   POST /v1/pc/enterprise/salary/calculate — 自动薪资核算
   - 根据已确认工时计算: 应发薪资 = 时薪 × 工时
   - 累计预扣法计算个税: 累计应纳税所得额 × 税率 - 速算扣除数 - 累计已预扣税额
   - 实发薪资 = 应发薪资 - 个税
   - 生成t_salary_flow记录

   个税累计预扣法（简化版）:
   - 累计收入 ≤ 60000: 税率0%
   - 累计收入 ≤ 96000: 税率3%，速算扣除数0
   - 累计收入 ≤ 204000: 税率10%，速算扣除数2520
   - （兼职收入按劳务报酬，此处简化按工资薪金预扣）

C. 薪资发放
   POST /v1/enterprise/salary/pay — 发起薪资发放
   - 校验: 托管账户可用余额 >= 应发总金额
   - 校验: flow_id幂等（已在t_salary_flow中存在则返回409）
   - 从托管账户冻结对应金额
   - 调用微信支付/支付宝企业付款到个人API
   - 成功: 解冻→发放，更新flow状态=3（已到账）
   - 失败: 解冻，flow状态=4（已驳回），通知财务管理员

   POST /v1/pc/admin/finance/settlement/pay — 批量薪资代发（财务管理员）

D. 支付回调幂等性处理
   - 基于flow_id + Redis分布式锁实现
   - 回调先查Redis: pay:callback:{flow_id}
   - 存在则直接返回成功（不重复处理）
   - 不存在则处理+写Redis(setex 86400)

E. 薪资流水查询
   GET /v1/student/salary/flow — 学生薪资流水
   - 参数: month(可选), page, size
   - 返回: 应发/实发/个税/状态/到账时间
   GET /v1/enterprise/salary/records — 企业薪资发放记录

F. 五流合一数据关联
   - 每笔薪资流水关联:
     agreement_id（合同流）+ salary_flow_id（资金流）+ invoice_id（发票流）+
     job_id（业务流）+ trace_id（数据流）
```

**提示词1.9 — 创建complaint-service + notification-service：**

```
你是Spring Boot微服务开发专家。请创建complaint-service和notification-service：

【complaint-service 端口8087】
A. 投诉CRUD
   POST /v1/student/complaint — 发起投诉
   - 接收: defendant_id, defendant_type, job_id, complaint_type, complaint_content, evidence_urls
   - 生成complaint_id，status=0（待审核）
   - 通知风控管理员（通过RabbitMQ）

   GET /v1/student/complaint/list — 查看我的投诉
   GET /v1/pc/admin/risk/complaint/list — 投诉工单列表（风控管理员）

B. 投诉处理
   POST /v1/pc/admin/risk/complaint/handle — 处理投诉
   - 接收: complaint_id, action(freeze/unfreeze/deduct/compensate), amount, handle_result
   - freeze: 冻结企业托管账户对应金额
   - unfreeze: 解冻
   - deduct: 直接划扣给学生
   - compensate: 平台先行赔付
   - 所有操作记录审计日志

C. 互评系统
   POST /v1/student/feedback — 学生评价企业/岗位
   POST /v1/enterprise/feedback — 企业评价学生
   - 1-5星评分
   - 评价内容存入t_practice_report表enterprise_comment字段
   - 影响双方信用分

【notification-service 端口8088】
A. RabbitMQ消息消费者
   - 监听队列: notification.sms（短信通知）
   - 监听队列: notification.im（IM推送）
   - 监听队列: notification.audit（审核通知）

B. 短信发送
   - 对接阿里云短信SDK 2.0.23
   - 模板: 面试提醒、薪资到账、审核结果、投诉处理进度

C. 站内信
   - 存储到t_notification表
   - 学生/企业查询: GET /v1/notification/list
   - 标记已读: PUT /v1/notification/read/{id}
```

#### Phase 6-7: 管理后台+第三方集成（第8-12周）

**提示词1.10 — 创建admin-service + data-service：**

```
你是管理后台Spring Boot开发专家。请创建admin-service和data-service：

【admin-service 端口8089】
需要实现AI驱动开发总纲.md第五章第5.6节全部管理后台接口。

A. 审核管理接口（角色: 审核管理员）
   GET /v1/pc/admin/enterprise/audit/list — 待审核企业列表
   POST /v1/pc/admin/enterprise/audit — 通过/驳回
   GET /v1/pc/admin/job/audit/list — 待审核岗位列表
   POST /v1/pc/admin/job/audit — 通过/下架

B. 风控管理接口（角色: 风控管理员）
   GET /v1/pc/admin/risk/complaint/list — 投诉工单列表
   POST /v1/pc/admin/risk/complaint/handle — 处理投诉
   GET /v1/pc/admin/risk/dashboard — 风控看板

C. 财务管理接口（角色: 财务管理员）
   GET /v1/pc/admin/finance/settlement/list — 待发放薪资列表
   POST /v1/pc/admin/finance/settlement/pay — 批量薪资代发
   GET /v1/pc/admin/finance/report — 财务报表

D. 运营管理接口（角色: 运营管理员）
   GET /v1/pc/admin/operation/report — 运营数据报表
   POST /v1/pc/admin/operation/notification — 推送通知

E. 超级管理接口（角色: 超级管理员）
   PUT /v1/pc/admin/system/config — 系统参数配置
   POST /v1/pc/admin/system/role — 角色权限管理
   GET /v1/pc/admin/audit-log/list — 操作审计日志

F. RBAC实现
   - 5类管理员角色: 1审核/2风控/3运营/4财务/5超级
   - 每个接口标注所需角色（见总纲第六章RBAC矩阵）
   - 超级管理员可分配子角色权限

G. IP白名单校验
   - 管理后台接口仅允许配置的白名单IP访问
   - IP白名单从系统配置表读取，超级管理员可修改

【data-service 端口8090】
A. 运营数据统计
   GET /v1/pc/admin/operation/report — 用户增长、岗位发布量、投递量、投诉量
   GET /v1/pc/enterprise/stat/dashboard — 企业招聘效果数据看板

B. 数据导出
   - 支持Excel导出（使用EasyExcel或Apache POI）
   - 导出: 用户列表、岗位列表、薪资流水、投诉记录、审计日志
```

**提示词1.11 — 第三方SDK集成：**

```
你是第三方服务集成专家。请完成以下SDK集成：

【电子签约 — 法大大/e签宝】
POST /v1/student/agreement/sign
- 对接e签宝SDK，调用 createContract 生成协议
- 协议模板: 工作内容、时长、薪资、结算方式、双方权责
- 生成PDF存储至OSS
- 学生人脸识别后签署
- 签署完成生成区块链存证哈希
- 企业端查看/下载已签协议

【视频面试 — TRTC】
- 对接腾讯云TRTC SDK
- 面试前15分钟推送提醒（IM+短信）
- 面试结束后标记状态

【短信通知 — 阿里云/腾讯云短信】
- 模板ID配置化管理
- 面试提醒: "【长沙兼职平台】{企业名}邀请您于{时间}参加面试"
- 薪资到账: "【长沙兼职平台】您的兼职薪资{金额}元已到账"
- 审核结果: "【长沙兼职平台】您的{企业认证/岗位}审核已{通过/未通过}"
```

---

## 五、Agent-2: 前端开发工程师

### 5.1 角色配置

```
角色名称: AI前端开发工程师
工作目录: miniapp/, pc-web/, h5-web/
依赖版本: 见第二章2.2节
参考文档:
  - AI驱动开发总纲.md (第五章API契约、第六章RBAC矩阵、第七章业务规则与色彩规范)
  - 模块开发设计框架/小程序端设计.md
  - 模块开发设计框架/网页端框架设计.md
  - 模块开发设计框架/pc端应用框架设计.md
  - 模块开发设计框架/UI设计框架.md
  - 模块开发设计框架/交互逻辑.md
```

### 5.2 开发提示词模板

#### Phase 1: 项目脚手架（第1-2周）

**提示词2.1 — 三端项目初始化：**

```
你是uni-app + Vue3前端开发专家。请初始化三端项目：

【任务】
A. 小程序端 (miniapp/)
   - 使用uni-app Vue2版本
   - 安装依赖: vant-weapp 1.11.5, @dcloudio/uni-ui 1.5.6, axios 0.27.2, dayjs 1.11.10, crypto-js 4.2.0
   - 配置manifest.json（微信+支付宝小程序appid暂用占位符）
   - 配置pages.json（TabBar: 首页/投递/消息/我的）
   - 创建目录结构:
     pages/student/    — 学生端页面
     pages/enterprise/ — 企业端页面
     components/       — 公共组件
     store/            — Vuex状态管理
     utils/            — 工具函数
     api/              — API接口封装

B. PC端 (pc-web/)
   - 使用Vue3 + Vite + TypeScript + Element Plus
   - 安装依赖: element-plus 2.4.4, pinia 2.1.7, echarts 5.4.3, vue-echarts 6.6.8, axios 1.6.3, dayjs 1.11.10, xlsx 0.18.5, crypto-js 4.2.0
   - 创建目录结构:
     src/views/enterprise/ — 企业运营端页面
     src/views/admin/      — 管理后台页面
     src/components/       — 公共组件
     src/stores/           — Pinia状态管理
     src/api/              — API接口封装
     src/router/           — 路由配置
     src/utils/            — 工具函数

C. H5端 (h5-web/)
   - 使用Vue3 + Vite + Vant 4.8.5
   - 安装依赖: vant 4.8.5, @vant/use 1.6.0, postcss-px-to-viewport-8-plugin 1.2.5, axios 1.6.3, dayjs 1.11.10
   - 配置viewport适配（设计稿375px）

D. 三端统一基础设施
   1. API请求封装 (api/request.js):
      - 自动注入Authorization头（从store读取token）
      - 自动注入X-Client-Type头
      - 自动注入X-Version头
      - 401响应自动跳转登录页
      - 403响应提示"无权限"
      - 500响应提示"系统繁忙"
      - 请求/响应拦截器统一错误处理

   2. 路由守卫:
      - 小程序: 路由跳转前检查登录态
      - PC端: router.beforeEach检查角色权限
      - H5端: 同上

   3. 全局样式变量 (styles/variables.scss):
      :root {
        --color-primary: #165DFF;    /* 平台蓝 */
        --color-accent: #FF7D00;     /* 活力橙 */
        --color-success: #00B42A;    /* 成功绿 */
        --color-danger: #F53F3F;     /* 警示红 */
        --color-bg: #FFFFFF;
        --color-card: #F2F3F5;
        --color-text: #4E5969;
        --color-text-secondary: #86909C;
        --font-size-sm: 12px;
        --font-size-base: 14px;
        --font-size-lg: 16px;
        --font-size-xl: 18px;
        --spacing-xs: 8px;
        --spacing-sm: 12px;
        --spacing-base: 16px;
        --spacing-lg: 24px;
        --radius-sm: 4px;
        --radius-base: 8px;
        --radius-lg: 12px;
        --btn-height-mobile: 44px;
        --btn-height-pc: 36px;
      }

【输出要求】
- 每个项目可独立 npm run dev 启动
- 提供README说明启动方式
- 所有路径使用@别名
```

#### Phase 2: 小程序学生端（第2-6周）

**提示词2.2 — 小程序首页：**

```
你是uni-app前端开发专家。请开发小程序学生端首页：

【页面路径】pages/student/index/index.vue

【功能要求】
A. 顶部区域
   - 安全警示滚动条: 固定顶部，红色背景(#FFF1F0)，红色文字(#F53F3F)
     滚动文案: "平台严禁收取任何押金、培训费、中介费！如遇收费请立即举报"
     使用swiper组件垂直滚动，每3秒切换
   - 搜索栏: 圆角输入框，placeholder"搜索兼职岗位/企业/商圈"
     点击跳转搜索页

B. 分类标签区
   - 横向滑动(scroll-view)的标签列表
   - 标签项: 全部/茶饮/零售/家教/会展/上门服务/新媒体/其他
   - 选中标签高亮(主色#165DFF)，默认选中"全部"

C. LBS推荐区
   - "附近好工作"标题 + "查看更多"链接
   - 前置检查: 无定位权限时显示"开启定位，查看附近兼职"按钮
   - 有定位时: 展示岗位卡片流（竖向列表）
   - 每个岗位卡片显示:
     · 岗位名称（16px加粗）
     · 企业名称 + "认证企业"标签（蓝色小标签）
     · 薪资（橙色#FF7D00大字）+ 薪资类型（时薪/日薪）
     · 结算方式标签（"日结"橙色、"周结"蓝色）
     · 工作地址 + 距离（如"2.3km"）
     · 技能要求标签（灰色小标签）
     · "含意外险"绿色盾牌图标
   - 下拉刷新、上滑加载更多（分页）

D. 安全提示固定区
   - 页面底部固定悬浮条
   - "本平台所有岗位支持薪资托管，无押金风险" + 盾牌图标

E. 数据流
   - 调用 GET /v1/student/job/list?longitude={}&latitude={}&distance=5&page=1&size=20
   - 分类筛选时添加industry_tag参数
   - 下拉刷新重新请求page=1
   - 使用Vuex store管理岗位列表数据

【样式要求】
- 主色 #165DFF，按钮高度不小于44px
- 卡片间距12px，页面左右边距16px
- 文本最小字号12px
- 符合微信小程序设计规范
```

**提示词2.3 — 岗位详情页：**

```
你是uni-app前端开发专家。请开发岗位详情页：

【页面路径】pages/student/job-detail/index.vue
【参数】job_id（通过URL参数传递）

【功能要求】
A. 页面布局（从上到下）
   1. 顶部岗位头图区（默认占位图或企业上传图）
   2. 岗位名称（18px加粗）+ 薪资（#FF7D00 20px加粗）
   3. 企业信息行: 企业名称 + "认证企业"标签 + 信用分徽章
   4. 岗位信息卡片（白底圆角8px）:
      - 工作地址（可点击调起地图导航）
      - 工作时间（格式化展示JSON）
      - 结算方式（日结/周结标签）
      - 招聘人数（已招/总数）
      - 技能要求
   5. 岗位描述（富文本展开）
   6. 安全区提示卡片（绿色边框）:
      - 盾牌图标 + "本岗位支持薪资托管，薪资由平台担保发放"
      - "含兼职意外险" 或 "建议企业购买意外险"
   7. 相似岗位推荐（横向滑动，最多5个）

B. 底部固定操作栏
   - 左侧: "收藏"按钮（星形图标，灰色）
   - 右侧: "立即投递"按钮（主色#165DFF填充，白色文字，全宽）
   - 点击投递:
     1. 检查登录态
     2. 检查实名认证状态（未实名→弹窗引导去认证）
     3. 二次确认弹窗: "已知晓岗位要求，确认岗位无押金/培训费等收费项目"
     4. 调用 POST /v1/student/apply {job_id}
     5. 成功→投递成功动效（简历飞入动画）+ 投递状态更新

C. 投递状态管理
   - 已投递: 按钮变为"已投递"（灰色，不可点击）
   - 已录用: 按钮变为"查看进度"（跳转投递详情）

【数据流】
- 页面加载: GET /v1/student/job/detail?job_id={}
- 投递: POST /v1/student/apply
- 收藏: POST /v1/student/favorite（暂存本地，二期做后端）
```

**提示词2.4 — 简历管理+投递流程：**

```
你是uni-app前端开发专家。请开发简历管理和投递流程页面：

【简历管理页】pages/student/resume/edit.vue
A. 基本信息编辑
   - 头像上传（调用uni.chooseImage）
   - 姓名、性别、学校（从认证信息自动带出，不可编辑）
   - 手机号（脱敏显示）

B. 可工作时间设置
   - 周一至周日7天选择器
   - 每天可选择上午/下午/晚间三个时段
   - 支持"同步课表"（拍照识别课表，AI解析后自动填充）
   - 存储格式: {"mon":["09:00-12:00","14:00-18:00"], ...}

C. 技能标签
   - 预设标签（横向排列，可多选）: PS/剪辑/文案/摄影/编程/英语/销售/客服/家教/服务员
   - 支持自定义输入新标签
   - 最多选择8个标签

D. 简历预览
   - 点击"预览"以卡片形式展示简历完整信息
   - 支持分享简历（生成小程序码）

【投递记录页】pages/student/apply/list.vue
A. Tab切换: 全部/已投递/待面试/已录用/已拒绝
B. 每个投递卡片显示:
   - 岗位名称 + 薪资
   - 企业名称
   - 投递时间
   - 状态标签（彩色标签区分）
   - 操作按钮: 取消投递（仅"已投递"状态）/ 查看详情
C. 点击卡片跳转岗位详情
D. 下拉刷新、上滑加载更多

【面试邀请页】（嵌入投递记录中）
- 显示面试时间、面试方式（视频/线下）
- "进入面试"按钮（视频面试时显示，临面试前15分钟才可点击）
- 调用TRTC SDK进入视频房间
```

**提示词2.5 — IM聊天+电子签约+GPS打卡：**

```
你是uni-app前端开发专家。请开发以下核心功能页面：

【IM聊天页】pages/student/im/chat.vue
A. 消息列表
   - 左右布局（自己消息靠右，对方消息靠左）
   - 消息气泡: 文本/图片/文件
   - 时间标签（今天显示时间、昨天显示"昨天"、更早显示日期）
   - 已读/未读状态
   - 滚动到底部

B. 输入区域
   - 文本输入框 + 发送按钮
   - 图片选择按钮（调用uni.chooseImage）
   - 文件选择按钮（调用uni.chooseFile）
   - 消息发送后自动滚动到底部

C. WebSocket连接
   - 页面onLoad时建立WebSocket连接
   - 连接URL: wss://api.parttime-cs.com/ws/im?token={JWT}
   - 收到消息自动追加到消息列表
   - 页面onUnload时断开连接
   - 使用uni.onSocketMessage监听

【电子签约页】pages/student/agreement/sign.vue
A. 协议预览
   - 嵌入web-view显示协议HTML内容
   - 显示: 岗位信息、薪资、工作内容、双方权责

B. 签署区域
   - Canvas手写签名板（白色底，支持保存和清除）
   - "我已阅读并同意协议内容"复选框（必勾选）
   - 人脸识别按钮（调用摄像头拍照）
   - "提交签署"按钮

C. 签署流程
   1. 预览协议 → 2. 手写签名 → 3. 人脸识别 → 4. 提交
   - 提交调用 POST /v1/student/agreement/sign
   - 成功后跳转协议列表页

【GPS打卡页】pages/student/clock/index.vue
A. 当前状态展示
   - 大圆形签到/签退按钮（未签到=蓝色、已签到未签退=橙色）
   - 显示当前时间
   - 显示当前位置（调用uni.getLocation获取GPS坐标）

B. 打卡操作
   - 签到: 点击按钮 → 获取GPS坐标 → 调用POST /v1/student/clock
     成功: GPS定位圈收缩动画 + "签到成功"提示
     距离超500m: 弹窗提示"当前位置距岗位地址较远，是否继续打卡？"
                   继续→标记异常打卡，需填写说明
   - 签退: 同签到逻辑

C. 打卡记录
   - 列表展示历史打卡记录
   - 正常打卡: 绿色圆点
   - 异常打卡: 红色圆点 + "申诉"入口
   - 点击申诉: 填写申诉原因 + 上传证据照片
```

**提示词2.6 — 薪资流水+安全中心+个人中心：**

```
你是uni-app前端开发专家。请开发以下页面：

【薪资流水页】pages/student/salary/index.vue
A. 顶部收入总览卡片
   - 本月收入（大字#FF7D00）
   - 累计收入
   - 待结算金额（灰色，待处理状态）

B. 月度筛选器
   - 月份左右滑动选择
   - 默认显示当前月份

C. 流水列表
   - 每条流水卡片:
     · 企业名称 + 岗位名称
     · 工作时长（X小时 × 时薪X元）
     · 应发/个税/实发金额
     · 结算状态标签: 待确认工时/待企业确认/待发放/已到账/已驳回
     · 到账时间（已到账状态时显示）
   - 点击查看详细薪资明细

【安全中心】pages/student/safety/index.vue
A. 安全警示滚动区
   - 典型案例卡片（图片+标题+概述）
   - 分类: 押金诈骗/刷单陷阱/信息泄露/虚假招聘

B. 防骗指南
   - 展开式FAQ（手风琴样式）
   - "如何识别虚假招聘？""遇到押金诈骗怎么办？"等

C. 一键举报入口
   - 红色大按钮（#F53F3F）"一键举报"
   - 点击跳转投诉页面

【投诉页面】pages/student/complaint/create.vue
A. 投诉表单
   - 选择投诉类型（虚假招聘/薪资拖欠/押金诈骗/未履约/信息泄露）
   - 关联岗位（从已投递岗位中选择，可选）
   - 被投诉人（自动带出或手动输入）
   - 投诉内容（多行文本，最少20字）
   - 证据上传（图片最多9张，支持拍照和相册选择）

B. 提交
   - 校验必填项
   - 调用POST /v1/student/complaint
   - 提交成功提示"投诉已提交，平台将在24小时内处理"

【个人中心】pages/student/mine/index.vue
A. 头部个人信息卡片
   - 头像 + 昵称 + 信用分（彩色进度条）
   - 认证状态标签

B. 功能入口（宫格布局，每行3个）
   - 我的简历 / 投递记录 / 薪资流水
   - 协议中心 / 保险记录 / 实践报告
   - 安全中心 / 投诉记录 / 设置

C. 退出登录
   - 底部按钮，清除token和用户信息
```

#### Phase 3-5: PC端 + H5端（第6-12周）

**提示词2.7 — PC企业运营端：**

```
你是Vue3 + Element Plus前端开发专家。请开发PC企业运营端：

【技术栈】
- Vue 3.3.13 + TypeScript 5.2.2 + Vite 5.0.10
- Element Plus 2.4.4 + Pinia 2.1.7 + ECharts 5.4.3
- 依赖版本严格使用以上版本

【整体布局】
采用经典后台布局:
┌──────────────────────────────┐
│ 顶部导航栏 (Logo + 企业名 + 消息 + 退出)     │
├────────┬─────────────────────┤
│ 左侧    │                     │
│ 导航    │    主工作区          │
│ 菜单    │    (router-view)     │
│        │                     │
├────────┴─────────────────────┤
│ 底部 (copyright)              │
└──────────────────────────────┘

左侧菜单:
- 数据看板
- 岗位管理
  - 发布岗位
  - 批量导入
  - 岗位列表
- 人才管理
  - 投递管理
  - 人才库
- 排班管理
- 薪资管理
  - 薪资核算
  - 发放记录
- 财务管理
  - 对账单
  - 发票管理
- 协议管理
- 设置

【关键页面要求】
A. 数据看板 (views/enterprise/dashboard/index.vue)
   - 4个统计卡片（岗位数/投递数/录用数/薪资总额）
   - ECharts折线图: 近30天投递趋势
   - ECharts饼图: 岗位类型分布
   - ECharts柱状图: 各岗位投递转化率
   - 支持日期范围筛选

B. 批量导入岗位 (views/enterprise/job/batch-import.vue)
   - 左侧: 模板下载 + 导入说明
   - 右侧: el-upload拖拽上传区域
   - 上传后显示导入进度条
   - 导入结果表格: 成功X条 / 失败X条，失败行标红+错误原因
   - 调用 POST /v1/pc/enterprise/job/batch-publish

C. 智能排班 (views/enterprise/schedule/index.vue)
   - 顶部: 月份选择器 + 排班统计
   - 中间: 日历视图（仿Google Calendar）
     每个日期显示已排班学生头像+姓名
     冲突时段红色标记
   - 右侧: 待排班学生列表（拖拽到日历）
   - 底部: 批量确认/导出排班表按钮
   - 使用 vue-draggable-plus 0.3.5 实现拖拽

D. 薪资核算 (views/enterprise/salary/calculate.vue)
   - 选择月份 → 加载该月考勤数据
   - 表格显示: 学生姓名/岗位/工时/时薪/应发/个税/实发/状态
   - 支持单个调整工时
   - 底部合计行
   - "确认发放"按钮 → 调用薪资发放接口

E. 人才库 (views/enterprise/talent-library/index.vue)
   - 搜索栏: 技能标签/信用分范围/合作次数
   - 学生卡片或表格展示
   - 操作: 发起邀请/加入岗位/查看简历

【路由守卫】
- 检查登录态 + 企业角色
- 未认证企业部分功能受限（显示"请先完成企业认证"）
```

**提示词2.8 — PC管理后台：**

```
你是Vue3 + Element Plus管理后台开发专家。请开发PC管理后台：

【路由结构】src/views/admin/
动态菜单根据管理员角色展示不同菜单项:

A. 审核管理 (role: 审核管理员)
   views/admin/audit/enterprise-list.vue — 企业审核列表
     - 表格: 企业名/信用代码/法人/提交时间/状态
     - 点击行→详情弹窗: 营业执照预览+天眼查核验结果
     - 操作: 通过（绿色按钮）/驳回（红色按钮+必填理由）
     
   views/admin/audit/job-list.vue — 岗位审核列表
     - 搜索: 关键词/行业/状态
     - AI审核结果列（敏感词标记红色）
     - 操作: 通过/下架+理由

B. 风控管理 (role: 风控管理员)
   views/admin/risk/complaint-list.vue — 投诉工单列表
     - 表格: 投诉人/被投诉人/类型/时间/状态
     - 点击行→详情: 投诉内容+证据展示（图片预览）+关联岗位+协议
     - 操作按钮:
       · 冻结资金 — 输入金额，确认冻结企业托管账户
       · 划扣补偿 — 输入金额，从冻结资金划给投诉人
       · 驳回投诉 — 填写理由
     - 操作记录timeline

   views/admin/risk/dashboard.vue — 风控看板
     - 实时统计: 今日投诉数/待处理数/冻结金额/已补偿金额
     - 投诉趋势图
     - 高风险企业/学生列表

C. 财务管理 (role: 财务管理员)
   views/admin/finance/settlement-list.vue — 待发放薪资列表
     - 表格: 企业/学生/岗位/金额/状态
     - 批量选中→"批量发放"→确认弹窗→调用支付接口
     - 发放进度实时展示

   views/admin/finance/report.vue — 财务报表
     - 筛选: 时间范围/企业/岗位类型
     - ECharts图表: 收入趋势/支出构成/毛利分析
     - 支持导出Excel

D. 运营管理 (role: 运营管理员)
   views/admin/operation/report.vue — 运营数据
   views/admin/operation/notification.vue — 推送管理

E. 超级管理 (role: 超级管理员)
   views/admin/system/config.vue — 系统配置
     - 表单: 时薪范围/推荐算法权重/信用分规则
   
   views/admin/system/roles.vue — 角色权限管理
     - 管理员列表 + 角色分配 + 权限树
   
   views/admin/system/audit-log.vue — 操作审计日志
     - 表格: 操作人/角色/模块/操作/IP/时间/参数
     - 搜索: 按角色/时间/模块
     - 导出Excel

【权限控制】
- 路由守卫: 检查admin_role_type，无权限菜单不显示
- 接口调用: 返回403时提示"无此操作权限"
```

---

## 六、Agent-3: 测试+运维工程师

### 6.1 角色配置

```
角色名称: AI测试+运维工程师
工作目录: deploy/, docker/, 项目根目录
依赖版本: 见第二章2.3节
参考文档:
  - AI驱动开发总纲.md (第八章Agent-3任务清单)
  - 详细分工.md (第三章人员3)
  - 模块开发设计框架/服务的部署平台的架构设计.md
  - 模块开发设计框架/第三方服务器选择方案.md
```

### 6.2 开发提示词模板

**提示词3.1 — Docker + 环境搭建：**

```
你是DevOps工程师。请完成以下任务：

【任务】
A. 编写 docker-compose.yml（根目录）
   一键启动所有基础设施服务:

   version: '3.8'
   services:
     mysql:
       image: mysql:8.0.35
       environment:
         MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
         MYSQL_DATABASE: parttime_platform
       ports: ["3306:3306"]
       volumes: ["./data/mysql:/var/lib/mysql"]
       command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
       healthcheck:
         test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
         interval: 10s
         retries: 5

     redis:
       image: redis:7.0.12-alpine
       command: redis-server --requirepass ${REDIS_PASSWORD} --appendonly yes
       ports: ["6379:6379"]
       volumes: ["./data/redis:/data"]

     elasticsearch:
       image: elasticsearch:7.17.20
       environment:
         - discovery.type=single-node
         - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
       ports: ["9200:9200", "9300:9300"]
       volumes: ["./data/es:/usr/share/elasticsearch/data"]

     rabbitmq:
       image: rabbitmq:3.12.10-management-alpine
       ports: ["5672:5672", "15672:15672"]
       environment:
         RABBITMQ_DEFAULT_USER: admin
         RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD}

     nginx:
       image: nginx:1.25.3-alpine
       ports: ["80:80", "443:443"]
       volumes: ["./nginx/nginx.conf:/etc/nginx/nginx.conf", "./nginx/ssl:/etc/nginx/ssl"]

B. 编写 Dockerfile.backend
   FROM amazoncorretto:8u392-alpine
   WORKDIR /app
   COPY target/*.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "app.jar"]

C. 编写 nginx.conf
   - HTTP自动跳转HTTPS
   - 反向代理到后端Gateway (8080)
   - 静态资源缓存 (js/css/图片 缓存30天)
   - 屏蔽敏感路径 (/actuator, /swagger-ui)
   - 限流: 单IP 100次/分钟
   - Gzip压缩

D. 编写 .env.example
   MYSQL_ROOT_PASSWORD=your_password
   REDIS_PASSWORD=your_password
   RABBITMQ_PASSWORD=your_password
   AES_SECRET_KEY=your_32byte_base64_key
   JWT_SECRET=your_jwt_secret
```

**提示词3.2 — 备份脚本+CI/CD：**

```
你是DevOps工程师。请编写以下脚本：

【任务】
A. 备份脚本 backup.sh
   #!/bin/bash
   # 每日凌晨2点全量备份MySQL到OSS
   # 每小时增量备份binlog到OSS
   # OSS开启版本控制
   # 保留最近30天备份，自动清理旧备份
   # 备份失败发送告警通知

B. Jenkinsfile
   pipeline {
     agent any
     stages {
       stage('Checkout') { steps { git '...' } }
       stage('Build Backend') { steps { sh 'mvn clean package -DskipTests' } }
       stage('Build Frontend') { steps { sh 'cd pc-web && npm ci && npm run build' } }
       stage('Test') { steps { sh 'mvn test' } }
       stage('Docker Build') { steps { sh 'docker build -t parttime-backend .' } }
       stage('Deploy') { steps { sh 'docker-compose up -d' } }
     }
     post {
       failure { emailext body: '构建失败', subject: 'CI失败通知', to: 'admin@xxx.com' }
     }
   }

C. 监控配置 prometheus.yml
   - 监控Spring Boot actuator指标
   - 监控MySQL/Redis/ES健康状态
   - 告警规则: CPU≥80%持续5分钟/内存≥90%/磁盘≥85%

D. Uptime Kuma配置
   - 监控所有微服务健康端点
   - 监控SSL证书到期时间
```

**提示词3.3 — 功能+安全+合规测试用例：**

```
你是QA测试工程师。请编写以下测试用例：

【测试框架】使用Postman Collection Runner + JMeter

【A. 功能测试用例（全流程闭环）】

TC-001: 学生注册登录流程
  1. POST /v1/auth/sms-code {phone} → 200
  2. POST /v1/auth/phone-login {phone, code} → 200, 返回token
  3. 用token调用 GET /v1/student/profile → 200
  预期: 注册成功后student_id不为空，verify_status=0

TC-002: 学生实名认证
  1. POST /v1/student/auth/register {real_name, student_no, school_id, id_card, phone}
  2. 验证: id_card字段在数据库中为AES加密密文（非明文）
  3. 验证: API返回的手机号中间4位为****
  预期: verify_status变为2（需mock学信网接口返回成功）

TC-003: 企业注册认证
  1. POST /v1/enterprise/auth/register {enterprise_name, credit_code, business_license}
  2. 验证: verify_status=1（审核中）
  3. 用审核管理员token调用 POST /v1/pc/admin/enterprise/audit 通过
  4. 验证: verify_status=2 且 is_certified=1

TC-004: 岗位发布+审核
  1. 认证企业发布岗位（含"押金"敏感词）
  2. 验证: status=0（待审核），不在公开列表显示
  3. 发布正常岗位（无敏感词，时薪≥17）
  4. 验证: status=1（已发布），可在公开列表查询到

TC-005: 投递+签约+打卡+结算（全流程）
  1. 学生投递岗位 → apply_status=0
  2. 企业标记"待面试" → apply_status=1
  3. 企业发起电子签约 → 学生签署 → sign_status=2
  4. 学生GPS打卡签到 → 签退 → 计算工时
  5. 企业确认工时 → 薪资核算 → 发起发放
  6. 学生查看薪资到账 → settlement_status=3
  预期: 全流程无断点，每个状态正确流转

TC-006: 薪资异常场景
  - 企业余额不足时发起发放 → 返回422
  - 同一flow_id重复发放 → 返回409（幂等性验证）
  - 支付回调丢失 → 定时任务查询支付结果并补单

TC-007: 投诉处理流程
  1. 学生发起投诉 → status=0
  2. 风控管理员受理 → status=1
  3. 冻结企业托管账户金额
  4. 调解完成 → 划扣/补偿 → status=2/3

【B. 安全测试用例】

TC-SEC-001: SQL注入测试
  - 所有GET请求参数注入: ?keyword=' OR '1'='1
  - 所有POST请求body注入: {"job_title": "'; DROP TABLE t_job; --"}
  预期: 所有接口返回400或正常处理，不执行注入SQL

TC-SEC-002: XSS攻击测试
  - 岗位描述输入: <script>alert('xss')</script>
  - 简历内容输入: <img src=x onerror=alert(1)>
  预期: HTML标签被转义或过滤

TC-SEC-003: 未授权访问测试
  - 用学生token调用 POST /v1/enterprise/job/publish → 403
  - 用企业token调用 POST /v1/pc/admin/enterprise/audit → 403
  - 不带token调用 POST /v1/student/apply → 401
  预期: 正确返回403/401

TC-SEC-004: JWT伪造测试
  - 修改JWT中role字段从student改为admin
  - 使用过期token
  预期: 返回401

TC-SEC-005: 接口限流测试
  - 快速连续请求同一接口30次
  预期: 超过限流阈值后返回429 Too Many Requests

【C. 合规测试用例】

TC-CPL-001: 敏感数据加密验证
  - 查询数据库t_student表，验证id_card_encrypt和phone_encrypt为密文
  - 查询API返回，验证手机号/身份证号脱敏展示

TC-CPL-002: 审计日志完整性
  - 执行审核/结算/投诉处理操作后
  - 查询t_audit_log表验证记录存在
  - 验证字段: operator_id, module, action, ip_address, client_type, trace_id

TC-CPL-003: 五流合一验证
  - 完成一笔薪资发放后
  - 验证agreement_id/salary_flow_id/invoice_id/job_id/trace_id五个字段关联关系

TC-CPL-004: HTTPS验证
  - 验证所有API请求强制使用HTTPS
  - 验证TLS版本≥1.2

【D. JMeter性能测试脚本】
  - 模拟1000并发用户
  - 场景1: 岗位搜索接口 → 响应时间≤200ms
  - 场景2: 投递接口 → 响应时间≤1s
  - 场景3: 登录接口 → 响应时间≤500ms
  - 持续时间: 5分钟
  - 验证: 错误率<1%
```

---

## 七、Agent-4: 产品经理+UI设计师

### 7.1 角色配置

```
角色名称: AI产品经理+UI设计师
工作目录: docs/, 设计输出目录
参考文档:
  - AI驱动开发总纲.md (第八、九、十章)
  - 详细分工.md (第四章人员4)
  - 需求分析.md
  - 服务总纲.md
  - 所有UI相关设计框架文档
```

### 7.2 开发提示词模板

**提示词4.1 — PRD生成：**

```
你是产品经理。请基于以下信息输出完整的PRD文档：

【参考文档】
- AI驱动开发总纲.md：包含完整的API契约、数据库Schema、RBAC权限矩阵、业务规则
- 需求分析.md：包含三类角色的完整权限说明
- 服务总纲.md：包含业务线、需求真伪审核、盈利模式

【输出要求】
A. 每个API接口补充完整的JSON请求示例和响应示例
   格式:
   ### POST /v1/student/job/list
   **请求示例**:
   ```json
   {
     "longitude": 112.9388,
     "latitude": 28.2282,
     "distance": 5,
     "salary_min": 17,
     "industry_tag": "茶饮",
     "settlement_type": 1,
     "page": 1,
     "size": 20
   }
   ```
   **响应示例**:
   ```json
   {
     "code": 200,
     "message": "成功",
     "data": {
       "list": [...],
       "total": 156,
       "page": 1,
       "size": 20
     }
   }
   ```

B. 核心业务流程图（Mermaid格式，可直接嵌入README）
   - 学生求职全流程
   - 企业招聘全流程
   - 薪资托管资金流向图
   - 投诉处理流程图

C. 需求优先级矩阵
   P0(MVP必须): 注册登录/实名认证/岗位发布浏览/投递/基础IM/
                电子签约/GPS打卡/薪资托管/基础审核
   P1(增长期): 批量导入/排班/薪资核算/人才库/数据看板/完整管理后台
   P2(成熟期): 实践报告/社区/SaaS工具/增值服务

D. 接口校验规则清单
   - 所有必填字段列表
   - 所有业务规则映射（时薪≥17、打卡距离≤500m、信用分0-200等）
```

**提示词4.2 — UI设计规范生成：**

```
你是UI/UX设计师。请生成以下设计规范文档：

【A. 色彩系统CSS变量】
:root {
  /* 主色系 */
  --color-primary: #165DFF;
  --color-primary-light: #3C7EFF;
  --color-primary-dark: #0E42D2;
  --color-primary-bg: #E8F0FE;

  /* 辅助色 */
  --color-accent: #FF7D00;
  --color-accent-light: #FF9A2E;

  /* 功能色 */
  --color-success: #00B42A;
  --color-success-bg: #E8FFEA;
  --color-danger: #F53F3F;
  --color-danger-bg: #FFF1F0;
  --color-warning: #FF7D00;

  /* 中性色 */
  --color-bg: #FFFFFF;
  --color-bg-secondary: #F2F3F5;
  --color-bg-tertiary: #E5E6EB;
  --color-text: #4E5969;
  --color-text-secondary: #86909C;
  --color-text-disabled: #C9CDD4;
  --color-border: #E5E6EB;
}

【B. 字体规范】
- 移动端: 基础14px, 标题16px/18px, 辅助12px, 数字20px
- PC端: 基础14px, 标题16px/18px/20px, 辅助12px
- 字体族: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif

【C. 间距规范】
- 移动端: 页面边距16px, 卡片内边距12px, 组件间距12px
- PC端: 页面边距24px, 卡片内边距16px, 组件间距16px

【D. 组件规范】
按钮:
- 主按钮: 主色填充, 白色文字, 圆角12px(移动端)/8px(PC端)
  高度: 移动端44px / PC端36px
- 次按钮: 主色边框, 透明背景
- 危险按钮: 危险色填充
- 禁用按钮: 灰色背景, 灰色文字

标签(Tag):
- 圆角4px, 内边距4px 8px
- 颜色: "日结"橙色 / "周结"蓝色 / "认证企业"蓝色 / "已录用"绿色 / "含意外险"绿色

卡片(Card):
- 白底, 圆角8px, 阴影: 0 2px 8px rgba(0,0,0,0.08)
- 移动端padding: 12px / PC端padding: 16px

输入框:
- 高度: 移动端44px / PC端36px
- 边框色: var(--color-border), 获焦色: var(--color-primary)

【E. 页面布局规范】
小程序端:
- 底部TabBar: 首页/投递/消息/我的（均使用线性图标）
- 页面内容区: 全宽, 左右边距16px
- 列表项高度≥44px（符合微信小程序规范）

PC端:
- 左侧导航宽度: 220px
- 顶部导航高度: 56px
- 主工作区最小宽度: 1024px
- 数据表格行高: 48px
```

**提示词4.3 — 页面原型描述（给前端AI开发用）：**

```
你是UI设计师。请用文字+ASCII描述每个页面的布局结构，
前端AI开发人员可直接根据此描述编写代码。

【描述格式】
每个页面包含:
1. 页面名称 + 路径
2. 页面结构（从上到下/从左到右描述）
3. 每个区块的内容+数据来源
4. 交互状态（加载中/空数据/错误/正常）
5. 间距和颜色

【需要生成原型的页面列表】

小程序端（20+页面）:
1. 首页 (pages/student/index)
2. 岗位详情 (pages/student/job-detail)
3. 筛选面板 (components/filter-panel) — 底部弹出
4. 简历编辑 (pages/student/resume/edit)
5. 投递记录 (pages/student/apply/list)
6. IM聊天 (pages/student/im/chat)
7. IM会话列表 (pages/student/im/conversation)
8. 电子签约 (pages/student/agreement/sign)
9. GPS打卡 (pages/student/clock/index)
10. 薪资流水 (pages/student/salary/index)
11. 安全中心 (pages/student/safety/index)
12. 投诉创建 (pages/student/complaint/create)
13. 个人中心 (pages/student/mine/index)
14-20. 企业端: 首页看板/岗位发布/简历查看/工时确认/薪资发放

PC企业运营端（15+页面）:
1. 整体布局框架
2. 数据看板
3. 岗位批量导入
4. 智能排班
5. 薪资核算
6. 人才库
7. 财务对账

PC管理后台（10+页面）:
1. 审核管理（企业+岗位）
2. 风控管理（投诉+看板）
3. 财务管理（结算+报表）
4. 运营管理（数据+推送）
5. 超级管理（配置+角色+审计）

【示例描述格式 — 小程序首页】:
+--------------------------------------------------+
| [安全警示滚动条 — 红底白字, 40px高]                   |
+--------------------------------------------------+
| [搜索栏 — 圆角44px, 灰底, 左icon+placeholder]       |
+--------------------------------------------------+
| [分类标签 — 横向滑动, 选中蓝底白字, 默认灰底灰字]       |
|  [全部] [茶饮] [零售] [家教] [会展] [上门服务] ...     |
+--------------------------------------------------+
| [LBS推荐区]                                       |
| "附近好工作" 标题(16px)               "查看更多>"(14px)|
| +---------------------------------------------+  |
| | [岗位卡片1]                                   |  |
| | 岗位名称(16px加粗)      ¥18/时(#FF7D00,20px)  |  |
| | 茶颜悦色 · [认证企业]标签 · 2.3km             |  |
| | [日结]标签 [含意外险]标签                     |  |
| | 技能: [无要求]标签                            |  |
| +---------------------------------------------+  |
| | [岗位卡片2] ...                              |  |
| +---------------------------------------------+  |
+--------------------------------------------------+
| [底部安全提示 — 固定悬浮, 浅蓝底, 盾牌icon+文字]      |
+--------------------------------------------------+
| [TabBar: 首页 | 投递 | 消息 | 我的]                |
+--------------------------------------------------+

状态:
- 加载中: 骨架屏(skeleton)占位
- 空数据: 空状态插图 + "暂无附近岗位" + "扩大搜索范围"按钮
- 未定位: 定位权限引导弹窗
- 网络错误: "网络异常，点击重试"
```

**提示词4.4 — 动效参数表：**

```
你是动效设计师。请输出所有动效的详细参数（前端可直接实现）：

【A. 投递成功动效】
触发: 点击"立即投递"按钮且接口返回成功
动画序列:
  1. 按钮缩小至95% (100ms, ease-in)
  2. 按钮变为成功绿色 (#00B42A) (200ms)
  3. 简历图标从按钮位置飞出，沿弧线飞向顶部企业图标 (600ms, ease-out)
  4. 企业图标接收后微微弹跳 (300ms, cubic-bezier(0.68, -0.55, 0.27, 1.55))
  5. 绿色对勾在中心放大出现 (300ms, ease-out)
CSS实现建议: CSS @keyframes + transform

【B. 薪资到账动效】
触发: 进入薪资流水页且存在"已到账"的新记录
动画序列:
  1. 金币图标从顶部下落 (500ms, ease-in, 带旋转360deg)
  2. 金币落地后弹跳2次 (200ms × 2, cubic-bezier bounce)
  3. 金额数字从0滚动到实际金额 (800ms, 使用countUp算法)
  4. 金额数字保持金色 (#FF7D00) 闪烁3次 (100ms × 3)
CSS+JS实现: requestAnimationFrame实现数字滚动

【C. 打卡成功动效】
触发: GPS打卡接口返回成功
动画序列:
  1. 打卡按钮中心出现定位圈（蓝色圆环，半径从0扩展至60px, 400ms, ease-out）
  2. 定位圈收缩至30px (200ms, ease-in)
  3. 中心出现白色对勾 (200ms, scale从0到1)
  4. 按钮颜色从蓝色变为绿色 (200ms)
  5. 顶部显示"签到成功"文字 (淡入, 100ms)
CSS实现建议: CSS animation + box-shadow

【D. 风险预警动效】
触发: 检测到敏感词或收到风险通知
动画序列:
  1. 红色边框闪烁 (红色#F53F3F与透明交替, 200ms × 5次)
  2. 内容区轻微左右震动 (translateX ±4px, 50ms × 6次)
  3. 移动端: 触发手机震动 (navigator.vibrate(200))
CSS实现建议: @keyframes shake + animation

【E. 页面过渡动效】
- 页面进入: 从右向左滑入 (300ms, ease-out), opacity 0→1
- 页面退出: 从左向右滑出 (200ms, ease-in), opacity 1→0
- 弹窗出现: scale 0.8→1 + opacity 0→1 (200ms, ease-out)
- 弹窗消失: scale 1→0.9 + opacity 1→0 (150ms, ease-in)
```

---

## 八、16周排期表

```
周次  | Agent-1(后端)                | Agent-2(前端)                | Agent-3(测试运维)           | Agent-4(产品UI)
------|-----------------------------|-----------------------------|---------------------------|------------------
W1    | Phase1: 父工程+common模块     | Phase1: 三端项目脚手架        | Phase1: Docker+环境搭建     | Phase1: PRD+API示例
W2    | Phase1: Gateway+JWT+加密     | Phase1: API封装+路由守卫      | Phase1: Nginx+HTTPS       | Phase2: UI规范+色彩
W3    | Phase2: user-service注册登录  | Phase2: 小程序首页+筛选        | Phase2: 备份脚本            | Phase3: 小程序首页原型
W4    | Phase2: user-service认证+简历 | Phase2: 岗位详情+投递流程      | Phase2: 测试框架搭建         | Phase3: 岗位详情+投递原型
W5    | Phase3: job-service CRUD     | Phase2: IM聊天+电子签约        | Phase3: 功能测试(随测)       | Phase3: IM+签约+打卡原型
W6    | Phase3: job-service ES搜索   | Phase2: GPS打卡+薪资流水      | Phase3: 功能测试            | Phase3: 薪资+安全中心原型
W7    | Phase4: match-service推荐    | Phase2: 安全中心+个人中心      | Phase3: 功能测试            | Phase4: PC企业端原型
W8    | Phase5: im-service           | Phase3: 小程序企业端          | Phase3: 支付异常测试         | Phase4: PC管理后台原型
W9    | Phase5: task-service打卡     | Phase4: PC批量导入+排班       | Phase4: 合规测试            | Phase5: H5端原型
W10   | Phase5: payment-service支付   | Phase4: PC薪资核算+人才库     | Phase4: 合规测试            | Phase5: H5端原型+动效
W11   | Phase6: admin-service审核    | Phase5: PC管理后台审核+风控   | Phase5: 安全测试            | Phase6: 动效参数表
W12   | Phase7: 第三方SDK集成         | Phase5: PC管理后台财务+超级   | Phase6: 兼容性测试          | Phase6: 动效参数表
W13   | 联调+Bug修复                  | Phase6: H5响应式适配         | 全量回归测试                | 功能验收
W14   | 联调+Bug修复                  | Bug修复+兼容性               | 压力测试                    | 验收+上线准备
W15   | 性能优化                      | UI体验优化                    | 监控配置                    | 上线材料
W16   | 性能优化                      | Bug修复                      | 部署上线                    | 校园推广启动
```

---

## 九、AI使用约定

### 9.1 每个Phase的AI工作流程

```
1. AI代理读取对应章节的提示词
2. AI代理理解需求后，先输出"实现计划"（列出要创建/修改的文件清单）
3. 人工确认计划无误后，AI逐文件生成代码
4. 代码生成后，AI执行编译/构建命令验证
5. 验证通过后，AI标记Phase完成，输出产物清单
```

### 9.2 禁止事项

```
- 禁止自行修改依赖版本（一切以第二章版本清单为准）
- 禁止跳过单元测试
- 禁止在代码中硬编码密钥/密码/Token
- 禁止在前端做敏感数据脱敏（必须后端统一脱敏）
- 禁止日志中打印身份证/手机号/密码明文
- 禁止GET请求传递敏感参数
- 禁止生成超过500行的方法（需拆分）
- 禁止使用float/double做金额计算（必须BigDecimal）
```

### 9.3 接口联调约定

```
- 后端实现接口后，立即更新Postman Collection并通知前端
- 前端发现接口与文档不符，在API文档对应位置标记评论
- 接口变更必须同步更新AI驱动开发总纲.md第五章
- 所有接口必须使用统一的响应格式 R.java
```

### 9.4 Git提交规范

```
分支命名: feature/{模块名}  例: feature/user-service
提交信息: <type>: <描述>
  feat: 新功能
  fix: 修Bug
  docs: 文档变更
  refactor: 重构
  test: 测试
  chore: 构建/工具
每天至少push一次，禁止本地囤积代码超过2天
```

### 9.5 AI代理提示词使用说明

```
1. 每个Phase开始前，将对应提示词复制给AI代理
2. AI代理输出代码时，人review后合并到主分支
3. 提示词中的【】标记为必须实现的内容
4. 提示词中的**标记为重要约束
5. 如有疑问，AI代理应主动询问而非自行假设
```

---

> **文档结束**
>
> 本文档配合 `AI驱动开发总纲.md` 使用。总纲定义"做什么"，本方案定义"怎么做+用什么版本+AI提示词"。
>
> 所有AI代理在开发前必须完整阅读本文档中自己对应的章节，然后严格按照Phase顺序执行。
