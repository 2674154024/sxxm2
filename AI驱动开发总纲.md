# 长沙大学生兼职平台 — AI驱动开发总纲

> **使用说明**：本文档为4人AI开发团队编写。每个AI代理读取对应角色章节即可独立工作。章节间通过「接口契约」和「数据库Schema」解耦。

---

## 一、项目概览

| 项 | 值 |
|---|---|
| 项目名称 | 长沙大学生兼职信息服务与保障平台 |
| 目标用户 | 长沙84万+在校大学生(C端) + 本地企业(B端) + 平台运营方 |
| 核心定位 | 安全、零押金、薪资托管的本地化兼职对接平台 |
| MVP周期 | 4个月 |
| 团队规模 | 4人(全部使用AI辅助开发) |

**三端部署**：
- 小程序端(微信+支付宝)：学生C端 + 企业B端轻量版
- PC Web端：企业运营端 + 平台管理后台
- H5移动端：学生拓展端(响应式)

---

## 二、技术栈(已确定，禁止更改)

### 后端
```
语言:        Java 8+
框架:        Spring Boot 2.7.x
微服务网关:   Spring Cloud Gateway
数据库:      MySQL 8.0
缓存:        Redis 7.0
搜索引擎:    Elasticsearch 7.17
对象存储:    Aliyun OSS / Tencent COS
消息队列:    RabbitMQ / RocketMQ
构建工具:    Maven
```

### 前端
```
小程序框架:   uni-app (Vue2) + Vant Weapp / uni-ui
PC端框架:     Vue3 + Element Plus + Pinia + ECharts
H5端框架:     Vue3 + Vant + postcss-px-to-viewport
```

### 第三方服务
```
支付:        微信支付API / 支付宝API
电子签约:    法大大 / e签宝 SDK
IM:          腾讯云IM SDK
视频面试:    腾讯云TRTC / 声网SDK
实名认证:    学信网接口 / 腾讯云实名认证
地图:        腾讯地图API / 高德地图API
短信:        阿里云短信 / 腾讯云短信
企业核验:    天眼查API
```

### DevOps
```
容器化:      Docker
CI/CD:       Jenkins
反向代理:    Nginx
SSL:         Let's Encrypt
监控:        云厂商免费监控 + Prometheus + Grafana + Uptime Kuma
```

---

## 三、项目目录结构(必须严格遵循)

```
part-time-platform/
├── backend/                          # 后端微服务(人员1负责)
│   ├── gateway/                      # Spring Cloud Gateway网关
│   ├── common/                       # 公共模块(工具类、异常处理、AES加密)
│   ├── user-service/                 # 用户服务(学生/企业注册、认证、信用分)
│   ├── job-service/                  # 岗位服务(发布、审核、搜索、推荐)
│   ├── match-service/                # 匹配服务(智能推荐算法)
│   ├── im-service/                   # 沟通服务(IM消息、面试管理)
│   ├── task-service/                 # 任务与考勤服务(打卡、工时计算)
│   ├── payment-service/              # 支付结算服务(薪资托管、核算、发放)
│   ├── complaint-service/            # 评价投诉服务
│   ├── notification-service/         # 消息通知服务(异步)
│   ├── data-service/                 # 数据服务(统计报表)
│   └── admin-service/                # 系统管理服务(RBAC权限)
│
├── miniapp/                          # 小程序端(人员2负责)
│   └── uni-app project
│       ├── pages/
│       │   ├── student/              # 学生端页面
│       │   └── enterprise/           # 企业端轻量页面
│       ├── components/               # 公共组件
│       ├── store/                    # Vuex状态管理
│       └── utils/                    # 工具函数、API封装
│
├── pc-web/                           # PC Web端(人员2负责)
│   ├── enterprise/                   # 企业运营端(Vue3)
│   └── admin/                        # 平台管理后台(Vue3)
│
├── h5-web/                           # H5移动端(人员2负责)
│   └── Vue3 + Vant项目
│
├── docs/                             # 本文档及设计文档
├── docker/                           # Docker配置
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   └── nginx.conf
├── deploy/                           # 部署脚本
│   ├── jenkinsfile
│   └── backup.sh
└── docker-compose.yml
```

---

## 四、数据库Schema(所有表完整DDL)

### 4.1 通用约定
- 所有表前缀 `t_`
- 主键统一 `id BIGINT AUTO_INCREMENT`
- 业务唯一标识统一 `xxx_id VARCHAR(64)` (UUID)
- 创建/更新时间：`create_time DATETIME`, `update_time DATETIME`
- 逻辑删除：`is_deleted TINYINT DEFAULT 0`
- 敏感字段加密存储(AES-256)，密钥在环境变量 `AES_SECRET_KEY`

### 4.2 建表语句

```sql
-- ========== 模块1: 用户与身份体系 ==========

CREATE TABLE t_student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL UNIQUE COMMENT '用户唯一标识',
    openid VARCHAR(128) COMMENT '小程序OpenID',
    real_name VARCHAR(32) COMMENT '真实姓名',
    gender TINYINT DEFAULT 0 COMMENT '0未知/1男/2女',
    school_id BIGINT COMMENT '所属高校ID',
    student_no VARCHAR(32) COMMENT '学号',
    verify_status TINYINT DEFAULT 0 COMMENT '0未核验/1核验中/2核验通过/3核验失败',
    id_card_encrypt VARCHAR(256) COMMENT '身份证号(AES加密)',
    phone_encrypt VARCHAR(128) COMMENT '手机号(AES加密)',
    available_time JSON COMMENT '可工作时间(JSON)',
    skill_tags VARCHAR(255) COMMENT '技能标签,逗号分隔',
    credit_score INT DEFAULT 100 COMMENT '信用分(0-200)',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_school_id (school_id),
    INDEX idx_verify_status (verify_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生用户表';

CREATE TABLE t_enterprise (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enterprise_id VARCHAR(64) NOT NULL UNIQUE COMMENT '企业唯一标识',
    enterprise_name VARCHAR(128) COMMENT '企业名称',
    credit_code VARCHAR(32) COMMENT '统一社会信用代码',
    business_license_url VARCHAR(255) COMMENT '营业执照OSS地址',
    verify_status TINYINT DEFAULT 0 COMMENT '0未提交/1审核中/2通过/3失败/4黑名单',
    legal_person VARCHAR(32) COMMENT '法人姓名',
    contact_name VARCHAR(32) COMMENT '联系人姓名',
    contact_phone_encrypt VARCHAR(128) COMMENT '联系人手机号(AES加密)',
    industry_tag VARCHAR(32) COMMENT '行业标签',
    credit_score INT DEFAULT 100 COMMENT '企业信用分(0-200)',
    is_certified TINYINT DEFAULT 0 COMMENT '0否/1是',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE UPDATE_TIME,
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_verify_status (verify_status),
    INDEX idx_industry_tag (industry_tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业用户表';

CREATE TABLE t_school (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_name VARCHAR(64) COMMENT '高校名称',
    address VARCHAR(128) COMMENT '高校地址',
    student_count INT COMMENT '在校生人数'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='高校信息表';

CREATE TABLE t_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id VARCHAR(64) NOT NULL UNIQUE COMMENT '管理员唯一标识',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '登录用户名',
    password_encrypt VARCHAR(256) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(32) COMMENT '真实姓名',
    role_type TINYINT NOT NULL COMMENT '1审核管理员/2风控管理员/3运营管理员/4财务管理员/5超级管理员',
    status TINYINT DEFAULT 1 COMMENT '0停用/1正常',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_role_type (role_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ========== 模块2: 岗位与匹配体系 ==========

CREATE TABLE t_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id VARCHAR(64) NOT NULL UNIQUE COMMENT '岗位唯一标识',
    enterprise_id VARCHAR(64) NOT NULL COMMENT '发布企业ID',
    job_title VARCHAR(128) COMMENT '岗位名称',
    job_type VARCHAR(32) COMMENT '岗位类型',
    industry_tag VARCHAR(32) COMMENT '行业标签',
    salary_type TINYINT COMMENT '1时薪/2日薪/3周薪',
    salary_amount DECIMAL(10,2) COMMENT '薪资标准',
    settlement_type TINYINT COMMENT '1日结/2周结/3月结',
    work_address VARCHAR(255) COMMENT '工作地址',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    work_time JSON COMMENT '工作时间要求(JSON)',
    skill_require VARCHAR(255) COMMENT '技能要求',
    job_desc TEXT COMMENT '岗位描述',
    recruit_num INT COMMENT '招聘人数',
    current_num INT DEFAULT 0 COMMENT '已录用人数',
    status TINYINT DEFAULT 0 COMMENT '0待审核/1已发布/2已下架/3已结束',
    is_insured TINYINT DEFAULT 0 COMMENT '0否/1是(含意外险)',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_job_id (job_id),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_status (status),
    INDEX idx_location (longitude, latitude),
    INDEX idx_salary (salary_type, salary_amount),
    INDEX idx_industry_tag (industry_tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兼职岗位表';

CREATE TABLE t_job_apply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    apply_id VARCHAR(64) NOT NULL UNIQUE COMMENT '投递唯一标识',
    job_id VARCHAR(64) NOT NULL COMMENT '岗位ID',
    student_id VARCHAR(64) NOT NULL COMMENT '学生ID',
    apply_status TINYINT DEFAULT 0 COMMENT '0已投递/1待面试/2已录用/3已拒绝/4已取消',
    interview_time DATETIME COMMENT '面试时间',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_apply_id (apply_id),
    INDEX idx_job_id (job_id),
    INDEX idx_student_id (student_id),
    INDEX idx_apply_status (apply_status),
    UNIQUE KEY uk_job_student (job_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位投递表';

CREATE TABLE t_job_match_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(64) NOT NULL COMMENT '学生ID',
    job_id VARCHAR(64) NOT NULL COMMENT '岗位ID',
    match_score DECIMAL(5,2) COMMENT '匹配得分',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推送推荐',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id),
    INDEX idx_job_id (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能匹配日志表';

-- ========== 模块3: 薪资与结算体系 ==========

CREATE TABLE t_electronic_agreement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agreement_id VARCHAR(64) NOT NULL UNIQUE COMMENT '协议唯一标识',
    job_id VARCHAR(64) NOT NULL COMMENT '岗位ID',
    student_id VARCHAR(64) NOT NULL COMMENT '学生ID',
    enterprise_id VARCHAR(64) NOT NULL COMMENT '企业ID',
    agreement_content TEXT COMMENT '协议内容',
    agreement_url VARCHAR(255) COMMENT '协议PDF OSS地址',
    sign_status TINYINT DEFAULT 0 COMMENT '0待学生签署/1待企业签署/2已签署/3已作废',
    sign_time DATETIME COMMENT '签署完成时间',
    blockchain_hash VARCHAR(256) COMMENT '区块链存证哈希',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_agreement_id (agreement_id),
    INDEX idx_job_id (job_id),
    INDEX idx_student_id (student_id),
    INDEX idx_enterprise_id (enterprise_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子协议表';

CREATE TABLE t_salary_escrow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    escrow_id VARCHAR(64) NOT NULL UNIQUE COMMENT '托管单唯一标识',
    enterprise_id VARCHAR(64) NOT NULL COMMENT '企业ID',
    job_id VARCHAR(64) COMMENT '岗位ID',
    total_amount DECIMAL(10,2) COMMENT '托管总金额',
    paid_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '已发放金额',
    freeze_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '冻结金额',
    status TINYINT DEFAULT 0 COMMENT '0待支付/1已托管/2已结清/3已退款',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_escrow_id (escrow_id),
    INDEX idx_enterprise_id (enterprise_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资托管表';

CREATE TABLE t_salary_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flow_id VARCHAR(64) NOT NULL UNIQUE COMMENT '流水唯一标识',
    student_id VARCHAR(64) NOT NULL COMMENT '学生ID',
    enterprise_id VARCHAR(64) NOT NULL COMMENT '企业ID',
    job_id VARCHAR(64) NOT NULL COMMENT '岗位ID',
    agreement_id VARCHAR(64) COMMENT '关联协议ID(合同流)',
    work_hours DECIMAL(10,2) COMMENT '工时',
    salary_amount DECIMAL(10,2) COMMENT '应发薪资',
    actual_amount DECIMAL(10,2) COMMENT '实发薪资(扣税后)',
    tax_amount DECIMAL(10,2) COMMENT '代扣个税',
    settlement_status TINYINT DEFAULT 0 COMMENT '0待确认工时/1待企业确认/2待平台发放/3已到账/4已驳回',
    pay_time DATETIME COMMENT '到账时间',
    trace_id VARCHAR(64) COMMENT '链路追踪ID(数据流)',
    invoice_id VARCHAR(64) COMMENT '关联发票ID(发票流)',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_flow_id (flow_id),
    INDEX idx_student_id (student_id),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_job_id (job_id),
    INDEX idx_settlement_status (settlement_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资流水表';

CREATE TABLE t_clock_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id VARCHAR(64) NOT NULL UNIQUE COMMENT '记录唯一标识',
    student_id VARCHAR(64) NOT NULL COMMENT '学生ID',
    job_id VARCHAR(64) NOT NULL COMMENT '岗位ID',
    clock_type TINYINT NOT NULL COMMENT '1签到/2签退',
    clock_time DATETIME NOT NULL COMMENT '打卡时间',
    clock_longitude DECIMAL(10,6) COMMENT '打卡经度',
    clock_latitude DECIMAL(10,6) COMMENT '打卡纬度',
    is_abnormal TINYINT DEFAULT 0 COMMENT '0正常/1异常(需申诉)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_record_id (record_id),
    INDEX idx_student_id (student_id),
    INDEX idx_job_id (job_id),
    INDEX idx_clock_time (clock_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- ========== 模块4: 安全与增值服务 ==========

CREATE TABLE t_complaint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id VARCHAR(64) NOT NULL UNIQUE COMMENT '投诉唯一标识',
    complainant_id VARCHAR(64) NOT NULL COMMENT '投诉人ID',
    complainant_type TINYINT NOT NULL COMMENT '1学生/2企业',
    defendant_id VARCHAR(64) NOT NULL COMMENT '被投诉人ID',
    defendant_type TINYINT NOT NULL COMMENT '1学生/2企业',
    job_id VARCHAR(64) COMMENT '关联岗位ID',
    complaint_type TINYINT COMMENT '1虚假招聘/2薪资拖欠/3押金诈骗/4未履约/5信息泄露',
    complaint_content TEXT COMMENT '投诉内容',
    evidence_urls VARCHAR(1024) COMMENT '证据OSS地址(JSON数组)',
    status TINYINT DEFAULT 0 COMMENT '0待审核/1处理中/2已调解/3已结案',
    handler_id VARCHAR(64) COMMENT '处理人ID',
    handle_result TEXT COMMENT '处理结果',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_complaint_id (complaint_id),
    INDEX idx_complainant_id (complainant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉纠纷表';

CREATE TABLE t_insurance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insurance_id VARCHAR(64) NOT NULL UNIQUE COMMENT '保险唯一标识',
    student_id VARCHAR(64) NOT NULL COMMENT '学生ID',
    job_id VARCHAR(64) NOT NULL COMMENT '岗位ID',
    enterprise_id VARCHAR(64) NOT NULL COMMENT '投保企业ID',
    insurance_type VARCHAR(32) COMMENT '保险类型',
    start_time DATETIME COMMENT '生效时间',
    end_time DATETIME COMMENT '结束时间',
    premium DECIMAL(10,2) COMMENT '保费',
    status TINYINT DEFAULT 0 COMMENT '0待生效/1生效中/2已过期/3已理赔',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_insurance_id (insurance_id),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兼职意外险表';

CREATE TABLE t_practice_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id VARCHAR(64) NOT NULL UNIQUE COMMENT '报告唯一标识',
    student_id VARCHAR(64) NOT NULL COMMENT '学生ID',
    job_id VARCHAR(64) NOT NULL COMMENT '岗位ID',
    enterprise_id VARCHAR(64) NOT NULL COMMENT '企业ID',
    work_content TEXT COMMENT '工作内容',
    work_duration INT COMMENT '工作时长(小时)',
    enterprise_comment TEXT COMMENT '企业评价',
    report_url VARCHAR(255) COMMENT '报告OSS地址',
    is_deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report_id (report_id),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实践认证报告表';

-- ========== 通用: 操作审计日志(合规必须) ==========

CREATE TABLE t_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id VARCHAR(64) NOT NULL COMMENT '操作人ID',
    operator_role TINYINT COMMENT '操作人角色',
    module VARCHAR(64) COMMENT '操作模块',
    action VARCHAR(64) COMMENT '操作类型',
    request_params TEXT COMMENT '请求参数(脱敏后)',
    ip_address VARCHAR(64) COMMENT '操作IP',
    client_type VARCHAR(32) COMMENT '端类型(miniapp/h5/pc-enterprise/pc-admin)',
    trace_id VARCHAR(64) COMMENT '链路追踪ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operator_id (operator_id),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志表(保留3年以上)';
```

---

## 五、API接口契约(全端统一)

### 5.1 通用规范

**Base URL**: `https://api.parttime-cs.com`

**请求头(必填)**:
```
Authorization: Bearer {JWT_TOKEN}
X-Client-Type: miniapp | h5 | pc-enterprise | pc-admin
X-Version: v1
Content-Type: application/json
```

**统一响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "trace_id": "uuid-string"
}
```

**错误码规范**:
| code | 含义 | 触发场景 |
|------|------|---------|
| 200 | 成功 | - |
| 400 | 参数错误 | 缺少必填字段、格式不符 |
| 401 | 未登录 | 令牌无效/过期 |
| 403 | 无权限 | 角色不匹配 |
| 409 | 业务冲突 | 重复投递、薪资已发放 |
| 422 | 业务校验失败 | 企业未认证、岗位含敏感词、薪资<17元 |
| 500 | 系统错误 | 数据库异常、第三方服务失败 |

**JWT令牌结构**:
```json
{
  "user_id": "xxx",
  "role": "student | enterprise | admin",
  "admin_role_type": 1,  // 仅管理员: 1审核/2风控/3运营/4财务/5超级
  "permissions": ["job:read", "job:apply"],
  "exp": 604800  // 7天
}
```

### 5.2 公开接口(无需认证)

| 方法 | 路径 | 说明 | 请求参数 | 响应data |
|------|------|------|---------|---------|
| POST | `/v1/auth/wechat-login` | 微信登录 | `{code}` | `{token, userInfo}` |
| POST | `/v1/auth/sms-code` | 发送验证码 | `{phone}` | `{}` |
| POST | `/v1/auth/phone-login` | 手机号登录 | `{phone, code}` | `{token, userInfo}` |
| GET | `/v1/public/job/list` | 公开岗位列表 | `?page=1&size=20` | `{list, total}` |

### 5.3 学生端接口(角色: student)

| 方法 | 路径 | 说明 | 关键校验 |
|------|------|------|---------|
| POST | `/v1/student/auth/register` | 注册+学籍核验 | 学信网核验通过才生成student_id |
| GET | `/v1/student/job/list` | 岗位列表(LBS+筛选) | `?longitude=&latitude=&salary_min=17&salary_max=&settlement_type=&distance=5` |
| GET | `/v1/student/job/detail` | 岗位详情 | `?job_id=` 企业联系方式脱敏 |
| POST | `/v1/student/resume` | 创建/更新简历 | 可工作时间存JSON |
| GET | `/v1/student/resume` | 查看简历 | |
| POST | `/v1/student/apply` | 投递岗位 | 校验:已实名+岗位未下架+未重复投递 |
| GET | `/v1/student/apply/list` | 投递记录 | `?status=` |
| GET | `/v1/student/im/messages` | IM消息记录 | `?target_id=&page=` |
| POST | `/v1/student/im/send` | 发送IM消息 | WebSocket长连接 |
| POST | `/v1/student/agreement/sign` | 签署电子协议 | 需人脸识别二次验证 |
| GET | `/v1/student/agreement/list` | 协议列表 | |
| POST | `/v1/student/clock` | GPS打卡 | 坐标与岗位误差≤500米 |
| GET | `/v1/student/clock/records` | 打卡记录 | `?job_id=` |
| GET | `/v1/student/salary/flow` | 薪资流水 | `?month=` |
| POST | `/v1/student/complaint` | 发起投诉 | 可上传证据 |
| GET | `/v1/student/complaint/list` | 投诉记录 | |
| POST | `/v1/student/report/generate` | 生成实践报告 | 付费可选 |
| PUT | `/v1/student/profile` | 修改个人信息 | |
| POST | `/v1/student/feedback` | 评价企业/岗位 | |

### 5.4 企业端接口(角色: enterprise)

| 方法 | 路径 | 说明 | 关键校验 |
|------|------|------|---------|
| POST | `/v1/enterprise/auth/register` | 注册+提交资质 | 上传营业执照，OCR识别 |
| GET | `/v1/enterprise/auth/status` | 查看认证状态 | |
| POST | `/v1/enterprise/job/publish` | 发布岗位 | 敏感词检测+仅认证企业 |
| PUT | `/v1/enterprise/job/update` | 编辑岗位 | |
| PUT | `/v1/enterprise/job/offline` | 下架岗位 | |
| GET | `/v1/enterprise/applicant/list` | 查看投递简历 | `?job_id=&status=` 学生手机号脱敏 |
| PUT | `/v1/enterprise/applicant/status` | 标记候选人状态 | 感兴趣/不合适/待面试 |
| POST | `/v1/enterprise/invite-interview` | 邀请面试 | `{apply_id, interview_time}` |
| POST | `/v1/enterprise/attendance/confirm` | 确认工时 | `{student_id, job_id, work_hours}` |
| POST | `/v1/enterprise/salary/pay` | 发起薪资发放 | 托管账户余额校验 |
| GET | `/v1/enterprise/salary/records` | 薪资发放记录 | |
| POST | `/v1/enterprise/complaint` | 发起投诉 | |
| GET | `/v1/enterprise/talent-library` | 人才库检索 | |

### 5.5 企业PC端接口(角色: enterprise, 仅pc-enterprise可调用)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/pc/enterprise/job/batch-publish` | 批量导入岗位(Excel) |
| GET | `/v1/pc/enterprise/schedule` | 查看排班表 |
| POST | `/v1/pc/enterprise/schedule` | 编辑排班表 |
| POST | `/v1/pc/enterprise/salary/calculate` | 自动薪资核算 |
| GET | `/v1/pc/enterprise/finance/bill` | 财务对账单 |
| POST | `/v1/pc/enterprise/saas/subscribe` | 订阅SaaS服务 |
| GET | `/v1/pc/enterprise/stat/dashboard` | 数据看板 |

### 5.6 管理后台接口(角色: admin)

| 方法 | 路径 | 权限角色 | 说明 |
|------|------|---------|------|
| GET | `/v1/pc/admin/enterprise/audit/list` | 审核 | 待审核企业列表 |
| POST | `/v1/pc/admin/enterprise/audit` | 审核 | 通过/驳回(需填理由) |
| GET | `/v1/pc/admin/job/audit/list` | 审核 | 待审核岗位列表 |
| POST | `/v1/pc/admin/job/audit` | 审核 | 通过/下架 |
| GET | `/v1/pc/admin/risk/complaint/list` | 风控 | 投诉工单列表 |
| POST | `/v1/pc/admin/risk/complaint/handle` | 风控 | 处理投诉(冻结/划扣/补偿) |
| GET | `/v1/pc/admin/risk/dashboard` | 风控 | 风控看板 |
| GET | `/v1/pc/admin/finance/settlement/list` | 财务 | 待发放薪资列表 |
| POST | `/v1/pc/admin/finance/settlement/pay` | 财务 | 批量薪资代发 |
| GET | `/v1/pc/admin/finance/report` | 财务 | 财务报表 |
| GET | `/v1/pc/admin/operation/report` | 运营 | 运营数据报表 |
| POST | `/v1/pc/admin/operation/notification` | 运营 | 推送通知 |
| PUT | `/v1/pc/admin/system/config` | 超级 | 系统参数配置 |
| POST | `/v1/pc/admin/system/role` | 超级 | 角色权限管理 |
| GET | `/v1/pc/admin/audit-log/list` | 超级 | 操作审计日志 |

---

## 六、RBAC权限矩阵

| 操作 | student | enterprise | 审核 | 风控 | 运营 | 财务 | 超级 |
|------|:-------:|:----------:|:----:|:----:|:----:|:----:|:----:|
| 浏览岗位 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| 投递岗位 | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ |
| 发布岗位 | ✗ | ✓(已认证) | ✗ | ✗ | ✗ | ✗ | ✗ |
| IM沟通 | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| 电子签约 | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| GPS打卡 | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ |
| 确认工时 | ✗ | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| 发起薪资发放 | ✗ | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| 查看自己薪资 | ✓ | - | - | - | - | - | - |
| 企业资质审核 | ✗ | ✗ | ✓ | ✗ | ✗ | ✗ | ✓ |
| 岗位内容审核 | ✗ | ✗ | ✓ | ✗ | ✗ | ✗ | ✓ |
| 投诉处理 | ✗ | ✗ | ✗ | ✓ | ✗ | ✗ | ✓ |
| 资金冻结/划扣 | ✗ | ✗ | ✗ | ✓ | ✗ | ✗ | ✓ |
| 批量薪资代发 | ✗ | ✗ | ✗ | ✗ | ✗ | ✓ | ✓ |
| 财务报表 | ✗ | ✗ | ✗ | ✗ | ✗ | ✓ | ✓ |
| 运营数据 | ✗ | ✗ | ✗ | ✗ | ✓ | ✗ | ✓ |
| 系统配置 | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | ✓ |
| 查看全站日志 | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | ✓ |

---

## 七、全局常量与业务规则

```yaml
业务规则:
  长沙最低时薪: 17 元/小时
  长沙建议时薪范围: 17-21 元/小时
  岗位搜索默认半径: 5 km
  打卡坐标误差上限: 500 米
  学生初始信用分: 100
  企业初始信用分: 100
  信用分范围: 0-200
  JWT有效期: 7 天
  审计日志保留: 3 年
  IM消息保留: 3 年

接口限流:
  公开接口: 单IP 10次/分钟
  核心业务接口: 单用户 5次/分钟
  管理后台接口: 单管理员 20次/分钟
  Nginx全局: 单IP 100次/分钟

敏感词库(DFA算法):
  - 押金
  - 培训费
  - 中介费
  - 高薪零门槛
  - 垫资
  - 刷单
  - 先交钱

色彩规范:
  主色(平台蓝): #165DFF
  辅助色(活力橙): #FF7D00
  安全色(成功绿): #00B42A
  警示色(风险红): #F53F3F
  背景色: #FFFFFF
  卡片色: #F2F3F5
  正文色: #4E5969
  辅助文字: #86909C
```

---

## 八、4人AI团队任务分配

### Agent-1: 后端开发工程师

**工作目录**: `backend/`

**任务清单(按依赖顺序)**:

```
Phase 1 - 基础架构(第1-2周)
  [ ] 创建Spring Boot父工程，配置pom.xml依赖
  [ ] 实现common模块: AES256加密工具类、统一响应格式、全局异常处理
  [ ] 配置Spring Cloud Gateway: JWT鉴权过滤器、限流规则、CORS配置
  [ ] 配置MySQL连接、Redis连接、Elasticsearch连接
  [ ] 实现JWT令牌生成/验证工具
  [ ] 实现RBAC权限拦截器
  [ ] 实现操作审计日志AOP切面(@AuditLog注解)
  [ ] 实现DFA敏感词过滤器

Phase 2 - 用户服务(第2-3周)
  [ ] user-service: 学生注册/登录(对接学信网接口)
  [ ] user-service: 企业注册/资质提交(OCR识别营业执照)
  [ ] user-service: 对接天眼查API核验企业信用
  [ ] user-service: 动态信用评分计算定时任务
  [ ] user-service: 学生/企业/管理员CRUD

Phase 3 - 岗位服务(第3-4周)
  [ ] job-service: 岗位CRUD
  [ ] job-service: AI敏感词检测(调用DFA)+人工审核状态机
  [ ] job-service: Elasticsearch岗位索引同步(Canal/定时任务)
  [ ] job-service: LBS地理位置搜索(ES geo_distance)

Phase 4 - 匹配服务(第4-5周)
  [ ] match-service: 智能匹配算法(技能40%+时间30%+距离30%)
  [ ] match-service: 匹配日志记录

Phase 5 - 核心流程(第5-8周)
  [ ] im-service: WebSocket长连接管理+消息存储
  [ ] im-service: 对接腾讯云IM SDK
  [ ] task-service: GPS打卡+工时自动计算
  [ ] payment-service: 薪资托管账户管理
  [ ] payment-service: 对接微信支付/支付宝(统一下单+回调幂等)
  [ ] payment-service: 累计预扣法个税计算
  [ ] payment-service: 薪资批量发放
  [ ] complaint-service: 投诉CRUD+状态机
  [ ] notification-service: RabbitMQ异步消息处理

Phase 6 - 管理后台接口(第8-10周)
  [ ] admin-service: 5类管理员角色的全部后台接口
  [ ] admin-service: 数据统计报表(对接data-service)

Phase 7 - 第三方集成(第10-12周)
  [ ] 电子签约: 法大大/e签宝SDK集成
  [ ] 视频面试: TRTC/声网SDK集成
  [ ] 短信通知: 阿里云/腾讯云短信SDK

输出:
  - 每个微服务独立可部署的JAR包
  - Dockerfile(使用docker/Dockerfile.backend模板)
  - 每个接口的Postman Collection
  - 数据库初始化SQL脚本
```

### Agent-2: 前端开发工程师

**工作目录**: `miniapp/`, `pc-web/`, `h5-web/`

**任务清单(按依赖顺序)**:

```
Phase 1 - 项目脚手架(第1-2周)
  [ ] uni-app项目初始化, 配置Vant Weapp/uni-ui
  [ ] Vue3 PC端项目初始化, Element Plus, Pinia, ECharts
  [ ] Vue3 H5端项目初始化, Vant, postcss-px-to-viewport
  [ ] 三端统一API请求封装(axios/uni.request拦截器, token自动注入)
  [ ] 三端统一错误处理(401跳登录, 403提示无权限)
  [ ] 三端路由守卫(按角色)
  [ ] 全局样式变量(色彩/字体/间距CSS变量)

Phase 2 - 小程序学生端(第2-6周)
  [ ] 底部Tab导航框架(首页/投递/消息/我的)
  [ ] 首页: LBS推荐区+搜索栏+分类标签滑动+岗位卡片流+安全警示滚动条
  [ ] 岗位详情页: 认证标签+薪资+地点导航+安全区提示+投递按钮
  [ ] 筛选面板: 时薪范围/结算方式/工作时间/距离
  [ ] 简历管理页: 编辑简历+课表同步+技能标签
  [ ] 投递流程: 二次确认弹窗+投递状态跟踪+面试邀请
  [ ] IM聊天页: 消息列表+聊天界面(对接腾讯云IM SDK)
  [ ] 电子签约页: 协议预览+手写签名+人脸识别
  [ ] GPS打卡页: 自动定位+签到/签退+异常申诉
  [ ] 薪资流水页: 月度汇总+明细列表+到账状态
  [ ] 安全中心: 警示案例+一键举报
  [ ] 个人中心: 我的模块入口

Phase 3 - 小程序企业端(第6-8周)
  [ ] 底部Tab导航(首页/岗位/我的)
  [ ] 首页数据看板(待处理卡片)
  [ ] 岗位发布页(分步表单+敏感词实时检测+时薪建议提示)
  [ ] 简历查看页(脱敏展示+IM入口)
  [ ] 工时确认+薪资发放页

Phase 4 - PC企业运营端(第8-11周)
  [ ] 左侧导航+顶部操作栏+中间工作区布局
  [ ] 数据看板(ECharts图表)
  [ ] 岗位批量导入(Excel模板+拖拽上传+错误明细)
  [ ] 智能排班(日历拖拽+冲突预警)
  [ ] 薪资核算(自动计算+对账单)
  [ ] 人才库(多维筛选+标签管理+批量邀请)
  [ ] 财务对账+发票管理+协议归档

Phase 5 - PC管理后台(第10-12周)
  [ ] 按管理员子角色动态菜单
  [ ] 审核管理: 企业/岗位/学生审核列表+详情+通过/驳回
  [ ] 风控管理: 投诉工单+证据链+资金操作
  [ ] 运营管理: 用户统计+推送+内容管理
  [ ] 财务管理: 托管账户+批量发放+报表导出
  [ ] 超级管理: 系统配置+角色权限+运维监控
  [ ] 操作审计页: 日志表格+筛选+导出

Phase 6 - H5响应式端(第11-12周)
  [ ] 学生拓展端: 实践报告生成/分享+社区页
  [ ] 响应式适配(移动端/平板/PC三断点)

输出:
  - uni-app编译产物(微信/支付宝小程序)
  - Vue3 PC端构建产物
  - Vue3 H5端构建产物
  - 全局组件库文档
```

### Agent-3: 测试+运维工程师

**工作目录**: `deploy/`, `docker/`, 项目根目录测试配置

**任务清单(按依赖顺序)**:

```
Phase 1 - 环境搭建(第1-3周)
  [ ] 领取免费云服务器(ECS 1核2G), 配置安全组
  [ ] 安装Docker, Nginx, MySQL 8.0, Redis 7.0
  [ ] 配置Nginx反向代理+HTTPS(Let's Encrypt)
  [ ] 编写docker-compose.yml(一键启动所有服务)
  [ ] 编写后端Dockerfile
  [ ] 配置Jenkins CI/CD流水线
  [ ] 部署Spring Boot后端(单实例)
  [ ] 配置MySQL每日全量备份脚本(OSS)+每小时增量备份
  [ ] OSS版本控制开启

Phase 2 - 测试框架搭建(第2-4周)
  [ ] 编写API自动化测试脚本(Postman Collection Runner / pytest)
  [ ] 编写JMeter性能测试脚本(模拟1万用户并发)
  [ ] 配置SonarQube代码质量扫描

Phase 3 - 功能测试(随开发进行, 第4-12周)
  [ ] 测试全流程闭环: 学生注册→认证→投递→面试→签约→打卡→结算→评价
  [ ] 重点测试薪资异常场景:
      - 企业余额不足时发起发放
      - 支付回调丢失(幂等性)
      - 重复发放
      - 个税计算正确性
  [ ] 测试电子签约: 协议生成+哈希存证验证
  [ ] 测试投诉处理: 资金冻结+补偿流程

Phase 4 - 合规测试(第10-12周)
  [ ] 验证敏感数据AES-256加密存储(查数据库确认密文)
  [ ] 验证脱敏展示(查API返回确认手机号/身份证脱敏)
  [ ] 验证操作审计日志完整性(查t_audit_log)
  [ ] 验证五流合一数据关联(agreement_id/salary_flow_id/invoice_id/job_id/trace_id)
  [ ] 验证等保基础项: HTTPS+TLS1.2+数据备份+权限隔离

Phase 5 - 安全测试(第10-12周)
  [ ] SQL注入测试(所有查询接口)
  [ ] XSS攻击测试(岗位描述/简历输入)
  [ ] 未授权访问测试(学生调企业接口, 企业调管理员接口)
  [ ] JWT令牌伪造测试
  [ ] 接口限流测试(快速连续请求验证429)
  [ ] 支付接口重放攻击测试

Phase 6 - 兼容性测试(第11-12周)
  [ ] 小程序端: 微信/支付宝最新版
  [ ] PC端: Chrome/Edge/Safari近2年版本
  [ ] H5端: iOS Safari/Android Chrome

Phase 7 - 监控与运维(第8-12周)
  [ ] 配置云监控告警(CPU≥80%, 内存≥90%, 磁盘≥85%)
  [ ] 部署Uptime Kuma监控后端服务
  [ ] 编写故障演练脚本(模拟DB崩溃, RTO≤1小时验证)
  [ ] 编写《安全事件应急响应手册》

输出:
  - 测试报告(功能/性能/安全/合规/兼容性)
  - docker-compose.yml + 部署文档
  - 备份恢复脚本
  - 应急响应手册
```

### Agent-4: 产品经理+UI设计师(AI辅助)

**工作目录**: `docs/`, 设计稿输出目录(由AI生成)

**任务清单(按依赖顺序)**:

```
Phase 1 - PRD确认(第1-2周)
  [ ] AI辅助生成完整PRD文档(每个接口的请求/响应+校验规则)
      - 使用本文档第五章API契约, 补充每个接口的详细JSON示例
  [ ] AI辅助生成核心业务流程图(Mermaid格式):
      - 学生求职全流程
      - 企业招聘全流程
      - 平台审核/风控/财务流程
      - 薪资托管资金流向图
  [ ] 需求优先级梳理(MVP vs 二期)

Phase 2 - UI设计规范(第1-2周)
  [ ] AI生成色彩体系CSS变量文件
  [ ] AI生成字体间距规范文档
  [ ] AI生成图标库选型清单(iconfont)
  [ ] AI生成按钮/标签/卡片/表单组件规范文档

Phase 3 - 小程序端UI(第2-5周)
  [ ] AI生成学生端20+页面线框图/高保真原型(Figma/AI工具)
      首页/岗位详情/投递/薪资/安全中心/个人中心/IM聊天
  [ ] AI生成企业端轻量页面
      首页看板/岗位发布/简历查看/薪资发放
  [ ] 输出页面切图标注(组件层级+数据绑定说明)

Phase 4 - PC端UI(第4-8周)
  [ ] AI生成企业运营端15+页面原型
      批量导入/排班/数据看板/财务对账/人才库
  [ ] AI生成管理后台10+页面原型
      审核列表/投诉处理/财务报表/系统配置

Phase 5 - H5端UI(第6-8周)
  [ ] AI生成响应式适配原型(3个断点)

Phase 6 - 动效设计(第8-10周)
  [ ] 投递成功动效: 简历飞入+绿色提示
  [ ] 薪资到账动效: 金币下落+金额滚动
  [ ] 打卡成功动效: GPS圈收缩
  [ ] 风险预警动效: 红色闪烁+震动

Phase 7 - 项目管理(全程)
  [ ] 每日站会(15分钟, 同步进度+阻塞项)
  [ ] 需求答疑(开发过程中澄清模糊需求)
  [ ] 对接长沙本地高校/企业做需求验证
  [ ] 启动资质办理: 《人力资源服务许可证》《ICP许可证》
  [ ] 冷启动准备: 校园大使海报+企业合作方案

输出:
  - PRD文档(含JSON示例)
  - 业务流程图(Mermaid格式, 可直接嵌入README)
  - UI设计规范文档(CSS变量可直接用)
  - 页面原型/切图标注
  - 动效参数表(给前端直接引用)
```

---

## 九、MVP开发排期(16周)

```
周次  | Agent-1(后端)        | Agent-2(前端)        | Agent-3(测试运维)   | Agent-4(产品UI)
------|---------------------|---------------------|-------------------|------------------
W1    | 基础架构搭建          | 三端项目脚手架       | 环境搭建           | PRD+UI规范
W2    | 网关+鉴权+加密       | API封装+路由守卫     | Docker+Nginx      | 小程序UI线框图
W3    | 用户服务(学生)       | 小程序学生首页       | 备份脚本           | 小程序UI高保真
W4    | 用户服务(企业)       | 岗位详情+投递流程    | 测试框架搭建       | 小程序UI高保真
W5    | 岗位服务(CRUD+审核)  | IM聊天+电子签约      | 功能测试(随测)     | PC企业端UI
W6    | 岗位服务(ES搜索)     | GPS打卡+薪资流水     | 功能测试           | PC企业端UI
W7    | 匹配服务(推荐算法)    | 安全中心+个人中心    | 功能测试           | PC管理后台UI
W8    | IM服务+打卡服务      | 小程序企业端        | 功能测试           | PC管理后台UI
W9    | 支付服务(托管+发放)   | PC企业端:批量+排班   | 性能测试           | H5端UI
W10   | 投诉服务+通知服务     | PC企业端:薪资+人才库  | 合规测试           | H5端UI+动效
W11   | 管理后台接口         | PC管理后台:审核+风控  | 安全测试           | 动效设计
W12   | 第三方SDK集成        | PC管理后台:财务+超级  | 兼容性测试         | 动效设计+联调
W13   | 联调+修bug          | H5响应式适配         | 全量回归测试       | 功能验收
W14   | 联调+修bug          | 修bug+兼容性        | 压力测试           | 验收+上线准备
W15   | 性能优化            | UI体验优化          | 监控配置           | 上线材料
W16   | 性能优化            | 修bug              | 部署上线           | 校园推广启动
```

---

## 十、开发约定(所有AI代理必须遵守)

### 10.1 命名规范
- Java类: PascalCase (`StudentController`)
- Java方法/变量: camelCase (`getStudentById`)
- 数据库表/字段: snake_case (`t_student`, `verify_status`)
- Vue组件: PascalCase (`JobDetail.vue`)
- Vue方法: camelCase (`handleSubmit`)
- API路径: kebab-case (`/v1/student/job/list`)
- JSON字段: snake_case (`{ "user_id": "xxx" }`)

### 10.2 代码规范
- 所有Java接口必须添加 `@AuditLog` 注解(自动记录操作审计)
- 所有涉及金额的计算使用 `BigDecimal`, 禁止使用 `float/double`
- 所有敏感字段(身份证、手机号、银行卡)存储前必须AES-256加密
- 所有API返回敏感字段必须脱敏(前端不做脱敏, 后端统一处理)
- 所有第三方API调用必须有超时设置(连接3s, 读取5s)和重试机制(最多3次)
- 支付相关接口必须实现幂等性(基于flow_id去重)

### 10.3 Git规范
- 分支: `feature/{模块名}` (如 `feature/user-service`)
- 提交信息: `<type>: <描述>` (如 `feat: 实现学生注册接口`)
- type: `feat`/`fix`/`docs`/`refactor`/`test`/`chore`
- 每天至少push一次, 禁止本地囤积代码超过2天

### 10.4 前后端接口联调
- 后端实现接口后立即更新Postman Collection, 通知前端
- 前端发现接口不符合文档, 直接在对应API文档处评论, 不私聊
- 接口变更必须同步更新本文档第五节

### 10.5 安全红线(违反即返工)
- 禁止在代码中硬编码密钥/密码
- 禁止在前端做敏感数据脱敏(必须后端脱敏)
- 禁止日志中打印身份证/手机号/密码明文
- 禁止GET请求传递敏感参数(放在Body或Header)
- 所有管理后台接口IP白名单校验

---

## 附录: 关键决策记录

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 跨端框架 | uni-app | 一套代码编译微信+支付宝, 开发成本低 |
| 后端框架 | Spring Boot 2.7 | 社区成熟, 匹配团队技能 |
| 数据库 | MySQL 8.0 | 免费, 事务支持好 |
| 搜索引擎 | Elasticsearch 7.17 | LBS+全文检索场景最佳 |
| 消息队列 | RabbitMQ | 轻量, 满足异步需求 |
| PC端框架 | Vue3+Element Plus | 国内生态好, 组件丰富 |
| 部署方式 | Docker+Jenkins | CI/CD自动化, 减少人工 |
| 服务器 | 免费云ECS | MVP阶段零成本 |
