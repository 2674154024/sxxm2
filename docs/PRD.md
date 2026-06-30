# 长沙大学生兼职平台 - 产品需求文档（PRD）

> **版本**: V1.3 | **日期**: 2026-06-30 | **状态**: 已发布
> **开发进度**: MVP开发完成，约90%完成

---

## 目录

1. [产品概述](#一产品概述)
2. [用户角色与人物画像](#二用户角色与人物画像)
3. [功能模块详细设计](#三功能模块详细设计)
4. [API接口契约（含JSON示例）](#四api接口契约含json示例)
5. [业务流程图](#五业务流程图)
6. [数据库Schema](#六数据库schema)
7. [非功能需求](#七非功能需求)
8. [MVP范围与验收标准](#八mvp范围与验收标准)
9. [开发偏差记录](#九开发偏差记录)
10. [盈利模式](#十盈利模式)
11. [合规与安全](#十一合规与安全)

---

## 一、产品概述

### 1.1 产品定位

长沙高校学生勤工俭学兼职服务平台，是一个面向长沙市84万+在校大学生的本地化兼职信息服务与权益保障平台。

**核心价值主张：**
- **安全**：严格企业资质审核、零押金保障、敏感词检测
- **高效**：智能人岗匹配、LBS附近推荐、全流程线上化
- **保障**：薪资托管担保、电子协议存证、兼职意外险

**差异化优势：**
- 长沙本地深耕，精准对接本地高校与企业
- 五流合一合规体系（合同流、资金流、发票流、业务流、数据流）
- 基于碎片化时间的智能匹配算法（技能40%+时间30%+距离30%）

### 1.2 目标用户

| 角色 | 数量规模 | 核心需求 |
|------|---------|---------|
| 大学生（求职者） | 84万+ | 安全岗位、及时结算、个人成长 |
| 企业方（招聘方） | 本地企业 | 精准匹配、流程自动化、风险规避 |
| 平台运营方（管理者） | 内部团队 | 审核监管、数据运营、财务合规 |

### 1.3 三端部署架构

| 端 | 技术栈 | 面向用户 | 核心功能 |
|----|--------|---------|---------|
| **小程序端** | uni-app + Vue3 + Pinia | 学生C端、企业B端轻量版 | 岗位浏览、投递、IM沟通、打卡、签约 |
| **PC Web端** | Vue3 + Element Plus | 企业运营端、平台管理后台 | 批量管理、数据看板、审核监管、财务结算 |
| **H5移动端** | Vue3 + Vant | 学生拓展端 | 实践报告、社区互动 |

### 1.4 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                    客户端层                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │ 小程序端  │  │  PC端    │  │  H5端    │  │ 管理后台 │ │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘ │
└───────┼──────────────┼──────────────┼──────────────┼──────┘
        │              │              │              │
┌───────▼──────────────▼──────────────▼──────────────▼──────┐
│                    网关层 (Spring Cloud Gateway)          │
│          JWT鉴权 · 限流 · CORS · 路由转发                 │
└──────────────────────┬───────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────┐
│                    微服务层                               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐    │
│  │user-service││job-service││match-service││im-service │    │
│  ├──────────┤ ├──────────┤ ├──────────┤ ├──────────┤    │
│  │task-service││payment-service││complaint-service│      │    │
│  │notification-service││admin-service││data-service│      │    │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘    │
└──────────────────────┬───────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────┐
│                    数据层                                 │
│  MySQL 8.0    Redis 7.0    Elasticsearch 7.17    RabbitMQ│
│  业务数据      缓存/会话    搜索/LBS     异步消息          │
└─────────────────────────────────────────────────────────┘
```

---

## 二、用户角色与人物画像

### 2.1 大学生（求职者）

**人物画像：**
- **年龄**：18-24岁，长沙在校大学生
- **动机**：积累社会经验（首要）、赚取零花钱、锻炼技能
- **痛点**：押金诈骗（38%遭遇过）、薪资拖欠（68.3%遭遇过）、信息泄露、岗位匹配度低
- **偏好**：日结/周结、时薪17-21元、附近岗位（5km内）

**核心功能权限：**

| 模块 | 功能 | 状态 | 用户故事 |
|------|------|------|---------|
| 浏览搜索 | 首页推荐、分类筛选、LBS附近岗位 | ✅ 已开发 | 作为学生，我希望浏览附近的兼职岗位，以便找到适合我的工作机会 |
| 简历管理 | 创建/编辑简历、课表同步、技能标签 | ✅ 已开发 | 作为学生，我希望管理我的简历，以便企业了解我的技能和可工作时间 |
| 投递申请 | 一键投递、状态跟踪、取消申请 | ✅ 已开发 | 作为学生，我希望投递心仪的岗位并跟踪申请进度 |
| 沟通面试 | IM即时沟通、视频面试、面试提醒 | ✅ 已开发 | 作为学生，我希望与企业在线沟通并参加视频面试 |
| 电子签约 | 在线签署协议、人脸识别 | ✅ 已开发 | 作为学生，我希望在线签署电子劳务协议，确保权益有保障 |
| 任务执行 | GPS打卡、工时确认、评价打分 | ✅ 已开发 | 作为学生，我希望通过平台打卡记录工时并评价工作体验 |
| 薪资保障 | 薪资流水、确认到账、保险记录 | ✅ 已开发 | 作为学生，我希望查看薪资明细并确认薪资到账 |
| 投诉维权 | 发起投诉、上传证据、处理进度 | ✅ 已开发 | 作为学生，我希望在遇到纠纷时发起投诉并查看处理进度 |
| 个人中心 | 资料修改、隐私设置、信用分 | ✅ 已开发 | 作为学生，我希望管理个人资料和查看信用评分 |

**受限/禁止权限：**
- 不得删除或伪造IM沟通记录（仅平台可审计调取，保留3年）
- 不得单方面修改已签署的电子合同
- 不得发布招聘岗位信息
- 不得查看其他学生的联系方式或隐私信息
- 不得提现他人账户薪资

### 2.2 企业方（招聘方）

**人物画像：**
- **类型**：长沙本地新消费品牌（茶颜悦色、零食很忙）、零售连锁、上门经济企业
- **需求**：快速招聘、降低管理成本、规避用工风险
- **痛点**：筛选效率低（平均匹配时长1.9天）、管理碎片化、纠纷处理难
- **偏好**：精准匹配、自动化工具、长期人才库

**核心功能权限：**

| 模块 | 功能 | 状态 | 用户故事 |
|------|------|------|---------|
| 企业认证 | 注册入驻、资质提交、信用评分 | ✅ 已开发 | 作为企业，我希望完成注册和资质认证，以便发布招聘信息 |
| 岗位管理 | 发布/编辑/下架岗位、数据查看 | ✅ 已开发 | 作为企业，我希望发布和管理兼职岗位并查看岗位数据 |
| 人才筛选 | 简历浏览、状态标记、批量下载 | ✅ 已开发 | 作为企业，我希望浏览投递简历并标记候选人状态 |
| 人才库 | 优秀学生收藏、标签搜索、批量邀请 | 🔄 开发中 | 作为企业，我希望建立人才库并向优秀学生发送邀请 |
| 沟通面试 | IM沟通、视频面试邀请 | ✅ 已开发 | 作为企业，我希望与候选人沟通并邀请面试 |
| 任务结算 | 工时确认、薪资核算、发放管理 | ✅ 已开发 | 作为企业，我希望确认工时并发放薪资 |
| SaaS工具 | 排班表、考勤统计、薪资自动核算 | 🔄 开发中 | 作为企业，我希望使用自动化工具管理排班和薪资 |
| 财务对账 | 佣金账单、开票申请、账户充值 | 🔄 开发中 | 作为企业，我希望查看财务账单并申请开票 |

**受限/禁止权限：**
- 不得在未经学生同意下查看其联系方式外的身份信息（手机号中间4位星号）
- 不得随意更改已确认的工时与薪资（需双方确认或申请平台介入）
- 不得发布超出营业执照经营范围或违法的兼职岗位
- 不得将平台内学生隐私信息导出用于其他商业用途

### 2.3 平台运营方（管理者）

**人物画像：**
- **子角色**：审核管理员、风控管理员、运营管理员、财务管理员、超级管理员
- **职责**：平台合规运营、风险管控、用户增长、财务结算

**子角色权限矩阵：**

| 子角色 | 核心功能 | 状态 | 用户故事 |
|--------|---------|------|---------|
| **审核管理员** | 企业资质审核、岗位内容审核、学籍核验 | ✅ 已开发 | 作为审核员，我希望审核企业资质和岗位信息，确保平台内容安全 |
| **风控管理员** | 风控预警看板、投诉纠纷处理、数据审计 | ✅ 已开发 | 作为风控员，我希望监控平台风险并处理投诉纠纷 |
| **运营管理员** | 用户数据统计、通知推送、内容管理 | ✅ 已开发 | 作为运营员，我希望查看运营数据并推送通知 |
| **财务管理员** | 薪资代发、发票管理、财务报表 | ✅ 已开发 | 作为财务员，我希望处理薪资发放和财务报表 |
| **超级管理员** | 系统配置、角色权限、运维监控 | ✅ 已开发 | 作为管理员，我希望配置系统参数和管理角色权限 |

**受限/禁止权限：**
- 后台所有管理员均不得自行修改个人薪资与结算数据
- 运营人员不得后台直接操作资金流转（仅财务可操作）
- 审核人员不得向外部泄露审核信息或在审核期外联系企业/学生
- 所有后台操作均需留痕，接受定期审计（审计日志保留3年以上）

### 2.4 权限关系总结

| 角色 | 核心活动 | 数据私密性 | 资金控制程度 | 平台风险责任 |
|------|---------|-----------|-------------|-------------|
| 学生（C端） | 申请、执行、评价 | 仅自己可见个人详情 | 只能查看自己的收入与流水 | 无 |
| 企业（B端） | 发布、筛选、结算 | 仅自己可见企业信息与岗位 | 可预付并确认发放薪资 | 需要为合作学生安全负责 |
| 平台（运营方） | 审核、监管、结算执行 | 全域可见（需严格遵守权限与合规） | 可进行托管资金的调拨与清分 | 承担全平台合规风险与最终调解责任 |

---

## 三、功能模块详细设计

### 3.1 小程序端 - 学生端

#### 3.1.1 首页模块

**页面路径**: `pages/student/index/index.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| 安全警示条 | 反诈骗提示滚动 | 红色背景，每3秒切换文案 | 滚动文案清晰可见，无遮挡 |
| 搜索栏 | 关键词搜索 | 支持岗位名称、企业、商圈 | 搜索结果与关键词匹配 |
| 分类导航 | 行业标签筛选 | 横向滑动，默认选中"全部" | 分类筛选生效，高亮显示 |
| LBS推荐区 | 附近岗位卡片流 | 下拉刷新、上滑加载更多 | 定位成功时显示附近岗位 |
| 安全提示区 | 薪资托管承诺 | 底部固定悬浮条 | 文字清晰，图标显示正常 |

**API调用：**
```
GET /v1/student/job/list?longitude={}&latitude={}&distance=5&page=1&size=20
```

#### 3.1.2 岗位详情页

**页面路径**: `pages/student/job-detail/index.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| 顶部头图 | 岗位/企业图片 | 默认占位图 | 图片加载正常 |
| 岗位信息 | 名称、薪资、企业认证标识 | 薪资橙色高亮 | 信息展示完整 |
| 岗位卡片 | 地址、时间、结算方式、招聘人数 | 地址可跳转地图 | 点击地址调起地图导航 |
| 岗位描述 | 富文本展开 | 支持收起/展开 | 展开/收起切换正常 |
| 安全提示 | 薪资托管、意外险标识 | 绿色边框卡片 | 标识清晰可见 |
| 相似推荐 | 横向滑动推荐 | 最多5个 | 推荐岗位加载正常 |
| 底部操作 | 收藏、立即投递 | 投递需二次确认 | 投递流程正常 |

**API调用：**
```
GET /v1/student/job/detail?job_id={}
POST /v1/student/apply
```

#### 3.1.3 简历管理页

**页面路径**: `pages/student/resume/edit.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| 基本信息 | 头像、姓名、性别、学校 | 学校从认证信息带出 | 信息展示正确 |
| 可工作时间 | 周一至周日时段选择 | 支持课表同步 | 时间选择生效 |
| 技能标签 | 预设标签+自定义 | 最多8个 | 标签添加/删除正常 |
| 预览按钮 | 卡片形式展示 | 支持分享 | 预览效果正确 |

**数据结构：**
```json
{
  "available_time": {
    "mon": ["09:00-12:00", "14:00-18:00"],
    "tue": ["14:00-18:00"],
    "wed": [],
    "thu": ["09:00-12:00"],
    "fri": ["14:00-18:00"],
    "sat": ["09:00-12:00", "14:00-18:00"],
    "sun": ["09:00-12:00"]
  },
  "skill_tags": ["PS", "剪辑", "英语"]
}
```

#### 3.1.4 投递记录页

**页面路径**: `pages/student/apply/list.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| Tab切换 | 全部/已投递/待面试/已录用/已拒绝 | 彩色状态标签 | Tab切换正常 |
| 投递卡片 | 岗位名称、薪资、企业、投递时间 | 点击跳转详情 | 状态展示正确 |
| 操作按钮 | 取消投递、查看详情 | 仅已投递状态可取消 | 取消操作生效 |

**API调用：**
```
GET /v1/student/apply/list?status={}
```

#### 3.1.5 IM聊天页

**页面路径**: `pages/student/im/chat.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| 消息列表 | 左右布局气泡 | 自己靠右、对方靠左 | 消息展示正确 |
| 时间标签 | 今天/昨天/日期 | 智能显示 | 时间格式正确 |
| 输入区域 | 文本、图片、文件 | 发送后自动滚动到底部 | 消息发送成功 |
| WebSocket | 实时消息推送 | 页面加载时连接 | 消息实时接收 |

**API调用：**
```
GET /v1/student/im/messages?target_id={}&page={}
POST /v1/student/im/send
```

#### 3.1.6 电子签约页

**页面路径**: `pages/student/agreement/sign.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| 协议预览 | web-view展示 | 岗位信息、薪资、权责 | 协议内容完整 |
| 签署区域 | Canvas手写签名板 | 支持保存和清除 | 签名保存成功 |
| 确认复选框 | 阅读并同意协议 | 必填项 | 未勾选无法提交 |
| 人脸识别 | 摄像头拍照验证 | 二次验证 | 人脸识别正常 |

**签署流程：**
1. 预览协议 → 2. 手写签名 → 3. 人脸识别 → 4. 提交签署（区块链存证）

**API调用：**
```
POST /v1/student/agreement/sign
```

#### 3.1.7 GPS打卡页

**页面路径**: `pages/student/clock/index.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| 当前状态 | 签到/签退大按钮 | 蓝色=未签到、橙色=已签到 | 状态显示正确 |
| 位置显示 | GPS坐标获取 | 调用uni.getLocation | 定位成功 |
| 打卡操作 | 签到/签退 | 距离超500m提示异常 | 距离校验生效 |
| 打卡记录 | 历史记录列表 | 绿色=正常、红色=异常 | 记录展示正确 |
| 申诉入口 | 异常打卡申诉 | 填写原因+上传证据 | 申诉提交成功 |

**核心规则：**
- 打卡坐标与岗位坐标误差≤500米（Haversine公式计算）
- 同一天同一岗位最多签到/签退各一次
- 签退时自动计算工时（精确到0.5小时，四舍五入）

**API调用：**
```
POST /v1/student/clock
GET /v1/student/clock/records
POST /v1/student/clock/appeal
```

#### 3.1.8 薪资流水页

**页面路径**: `pages/student/salary/index.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| 收入总览 | 本月收入、累计收入、待结算 | 大字橙色显示 | 金额计算正确 |
| 月度筛选 | 月份滑动选择 | 默认当前月份 | 筛选生效 |
| 流水列表 | 企业、岗位、工时、金额、状态 | 点击查看详情 | 流水展示完整 |

**状态流转：**
```
待确认工时 → 待企业确认 → 待平台发放 → 已到账/已驳回
```

**API调用：**
```
GET /v1/student/salary/flow?month={}
```

#### 3.1.9 安全中心

**页面路径**: `pages/student/safety/index.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| 警示案例 | 典型诈骗案例卡片 | 图片+标题+概述 | 案例展示正常 |
| 防骗指南 | 手风琴式FAQ | 展开/收起 | FAQ交互正常 |
| 一键举报 | 红色大按钮 | 跳转投诉页面 | 跳转正常 |

#### 3.1.10 投诉页面

**页面路径**: `pages/student/complaint/create.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| 投诉类型 | 单选下拉 | 虚假招聘/薪资拖欠/押金诈骗/未履约/信息泄露 | 类型选择生效 |
| 关联岗位 | 从已投递岗位选择 | 可选 | 岗位选择正常 |
| 投诉内容 | 多行文本 | 最少20字 | 字数校验生效 |
| 证据上传 | 图片最多9张 | 支持拍照和相册 | 上传成功 |

**API调用：**
```
POST /v1/student/complaint
```

#### 3.1.11 个人中心

**页面路径**: `pages/student/mine/index.vue`

**功能设计：**

| 区域 | 功能 | 交互说明 | 验收标准 |
|------|------|---------|---------|
| 个人信息卡片 | 头像、昵称、信用分、认证状态 | 信用分彩色进度条 | 信息展示正确 |
| 功能入口 | 九宫格布局 | 我的简历/投递记录/薪资流水/协议中心/保险记录/实践报告/安全中心/投诉记录/设置 | 入口跳转正常 |
| 退出登录 | 底部按钮 | 清除token和用户信息 | 退出成功 |

---

### 3.2 小程序端 - 企业端

#### 3.2.1 首页数据看板

**页面路径**: `pages/enterprise/index/index.vue`

**功能设计：**
- 待处理卡片：待审核岗位、待确认工时、待发放薪资
- 快捷入口：发布岗位、查看投递、查看人才库

#### 3.2.2 岗位发布页

**页面路径**: `pages/enterprise/job/publish.vue`

**功能设计：**

| 字段 | 类型 | 校验规则 | 验收标准 |
|------|------|---------|---------|
| 岗位名称 | 文本 | 必填，最大50字 | 必填校验生效 |
| 行业分类 | 下拉选择 | 必填 | 选择生效 |
| 薪资类型 | 单选 | 时薪/日薪/周薪 | 单选生效 |
| 薪资标准 | 数字 | ≥17元（长沙最低时薪） | 下限校验生效 |
| 结算方式 | 单选 | 日结/周结/月结 | 单选生效 |
| 工作地址 | 文本 | 必填 | 必填校验生效 |
| 工作时间 | JSON | 必填 | 时间选择生效 |
| 技能要求 | 文本 | 可选 | 输入正常 |
| 招聘人数 | 数字 | ≥1 | 下限校验生效 |
| 是否含意外险 | 开关 | 可选 | 开关生效 |

**API调用：**
```
POST /v1/enterprise/job/publish
```

#### 3.2.3 简历查看页

**页面路径**: `pages/enterprise/applicant/detail.vue`

**功能设计：**
- 学生信息脱敏展示（手机号中间4位星号）
- 技能标签、可工作时间
- IM沟通入口
- 状态标记：感兴趣/不合适/待面试

#### 3.2.4 工时确认页

**页面路径**: `pages/enterprise/attendance/confirm.vue`

**功能设计：**
- 打卡记录列表（正常/异常标记）
- 工时确认（可调整）
- 异常打卡审核

**API调用：**
```
POST /v1/enterprise/attendance/confirm
```

---

### 3.3 PC端 - 企业运营端

#### 3.3.1 数据看板

**页面路径**: `views/enterprise/dashboard/index.vue`

**功能设计：**
- 统计卡片：岗位数、投递数、录用数、薪资总额
- ECharts图表：近30天投递趋势、岗位类型分布、投递转化率
- 日期范围筛选

**API调用：**
```
GET /v1/pc/enterprise/stat/dashboard
```

#### 3.3.2 岗位批量导入

**页面路径**: `views/enterprise/job/batch-import.vue`

**功能设计：**
- 模板下载 + 导入说明
- el-upload拖拽上传区域
- 导入进度条
- 导入结果表格（成功/失败明细）

**API调用：**
```
POST /v1/pc/enterprise/job/batch-publish
```

#### 3.3.3 智能排班

**页面路径**: `views/enterprise/schedule/index.vue`

**功能设计：**
- 月份选择器 + 排班统计
- 日历视图（仿Google Calendar）
- 待排班学生列表（拖拽到日历）
- 冲突时段红色标记
- 批量确认/导出

#### 3.3.4 薪资核算

**页面路径**: `views/enterprise/salary/calculate.vue`

**功能设计：**
- 月份选择 → 加载考勤数据
- 表格：学生姓名/岗位/工时/时薪/应发/个税/实发/状态
- 支持单个调整工时
- 底部合计行

**API调用：**
```
POST /v1/pc/enterprise/salary/calculate
```

#### 3.3.5 人才库

**页面路径**: `views/enterprise/talent/library.vue`

**功能设计：**
- 多维筛选：技能、评价、历史合作次数
- 标签管理
- 批量邀请面试

---

### 3.4 PC端 - 平台管理后台

#### 3.4.1 审核管理

**页面路径**: `views/admin/audit/`

**功能设计：**

| 页面 | 功能 | 操作 | 验收标准 |
|------|------|------|---------|
| 企业审核列表 | 待审核企业列表 | 通过/驳回（需填理由） | 审核操作生效 |
| 岗位审核列表 | 待审核岗位列表 | 通过/下架（需填理由） | 审核操作生效 |

**API调用：**
```
GET /v1/pc/admin/enterprise/audit/list
POST /v1/pc/admin/enterprise/audit
GET /v1/pc/admin/job/audit/list
POST /v1/pc/admin/job/audit
```

#### 3.4.2 风控管理

**页面路径**: `views/admin/risk/`

**功能设计：**

| 页面 | 功能 | 操作 | 验收标准 |
|------|------|------|---------|
| 投诉工单列表 | 所有投诉记录 | 查看详情、处理投诉 | 处理操作生效 |
| 风控看板 | 实时数据看板 | 异常交易、高投诉岗位、批量注册 | 数据展示正确 |

**投诉处理操作：**
- freeze：冻结企业托管账户
- unfreeze：解冻账户
- deduct：划扣资金给学生
- compensate：平台先行赔付

**API调用：**
```
GET /v1/pc/admin/risk/complaint/list
POST /v1/pc/admin/risk/complaint/handle
GET /v1/pc/admin/risk/dashboard
```

#### 3.4.3 财务管理

**页面路径**: `views/admin/finance/`

**功能设计：**

| 页面 | 功能 | 操作 | 验收标准 |
|------|------|------|---------|
| 待发放薪资列表 | 待结算薪资总表 | 批量代发 | 发放操作生效 |
| 财务报表 | 日/周/月度报告 | 查看、导出 | 报表生成正确 |

**API调用：**
```
GET /v1/pc/admin/finance/settlement/list
POST /v1/pc/admin/finance/settlement/pay
GET /v1/pc/admin/finance/report
```

#### 3.4.4 运营管理

**页面路径**: `views/admin/operation/`

**功能设计：**

| 页面 | 功能 | 操作 | 验收标准 |
|------|------|------|---------|
| 运营数据报表 | 用户增长、活跃用户、留存率 | 查看、筛选 | 数据展示正确 |
| 推送管理 | 系统通知、活动公告、优惠券发放 | 创建、发送 | 推送操作生效 |

**API调用：**
```
GET /v1/pc/admin/operation/report
POST /v1/pc/admin/operation/notification
```

#### 3.4.5 超级管理

**页面路径**: `views/admin/system/`

**功能设计：**

| 页面 | 功能 | 操作 | 验收标准 |
|------|------|------|---------|
| 系统配置 | 时薪范围、推荐算法权重、信用评分规则 | 修改配置 | 配置保存生效 |
| 角色权限 | 创建/编辑/停用子角色账户 | 分配权限范围 | 权限分配生效 |
| 操作审计日志 | 全站操作日志 | 查看、筛选、导出 | 日志展示完整 |

**API调用：**
```
PUT /v1/pc/admin/system/config
POST /v1/pc/admin/system/role
GET /v1/pc/admin/audit-log/list
```

---

## 四、API接口契约（含JSON示例）

### 4.1 通用规范

**Base URL**: `https://api.parttime-cs.com`

**请求头**:
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
  "trace_id": "550e8400-e29b-41d4-a716-446655440000"
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
| 429 | 限流 | 请求频率超限 |
| 500 | 系统错误 | 数据库异常、第三方服务失败 |

**JWT令牌结构**:
```json
{
  "user_id": "user-123456",
  "role": "student",
  "admin_role_type": 1,
  "permissions": ["job:read", "job:apply"],
  "exp": 1700000000
}
```

### 4.2 公开接口

#### POST /v1/auth/wechat-login - 微信登录

**请求参数**:
```json
{
  "code": "071Xx00008kXkL1kR1kL1kXkXx00008k"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user_info": {
      "user_id": "student-8a2b3c",
      "real_name": "张三",
      "school_name": "长沙理工大学",
      "verify_status": 2,
      "credit_score": 100
    }
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### POST /v1/auth/sms-code - 发送验证码

**请求参数**:
```json
{
  "phone": "138****1234"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440001"
}
```

#### POST /v1/auth/phone-login - 手机号登录

**请求参数**:
```json
{
  "phone": "138****1234",
  "code": "123456"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user_info": {
      "user_id": "student-8a2b3c",
      "real_name": "张三",
      "school_name": "长沙理工大学",
      "verify_status": 2
    }
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440002"
}
```

#### GET /v1/public/job/list - 公开岗位列表

**请求参数**:
```
?page=1&size=20
```

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "job_id": "job-abc123",
        "job_title": "奶茶店店员",
        "salary_amount": 18.00,
        "salary_type": 1,
        "settlement_type": 1,
        "work_address": "长沙市岳麓区麓山南路",
        "distance": 2.3,
        "enterprise_name": "茶颜悦色",
        "is_certified": 1,
        "credit_score": 100,
        "is_insured": 1
      }
    ],
    "total": 100
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440003"
}
```

### 4.3 学生端接口

#### POST /v1/student/auth/register - 注册+学籍核验

**请求参数**:
```json
{
  "real_name": "张三",
  "student_no": "2023001234",
  "school_id": 1,
  "id_card": "4301********1234",
  "phone": "138****1234"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "user_id": "student-8a2b3c",
    "verify_status": 2
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440004"
}
```

#### GET /v1/student/job/list - 岗位列表(LBS+筛选)

**请求参数**:
```
?longitude=112.9399&latitude=28.2280&distance=5&salary_min=17&salary_max=30&settlement_type=1&industry_tag=茶饮&page=1&size=20
```

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "job_id": "job-abc123",
        "job_title": "奶茶店店员",
        "salary_amount": 18.00,
        "salary_type": 1,
        "settlement_type": 1,
        "work_address": "长沙市岳麓区麓山南路",
        "distance": 2.3,
        "enterprise_name": "茶颜悦色",
        "is_certified": 1,
        "credit_score": 100,
        "is_insured": 1,
        "industry_tag": "茶饮",
        "create_time": "2026-06-29 10:00:00"
      }
    ],
    "total": 50
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440005"
}
```

#### GET /v1/student/job/detail - 岗位详情

**请求参数**:
```
?job_id=job-abc123
```

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "job_id": "job-abc123",
    "job_title": "奶茶店店员",
    "job_type": "兼职",
    "industry_tag": "茶饮",
    "salary_type": 1,
    "salary_amount": 18.00,
    "settlement_type": 1,
    "work_address": "长沙市岳麓区麓山南路88号",
    "longitude": 112.9399,
    "latitude": 28.2280,
    "work_time": {
      "sat": ["09:00-18:00"],
      "sun": ["09:00-18:00"]
    },
    "skill_require": "沟通能力强、有服务意识",
    "job_desc": "负责奶茶制作、收银、门店卫生等工作",
    "recruit_num": 5,
    "current_num": 2,
    "status": 1,
    "is_insured": 1,
    "enterprise_info": {
      "enterprise_name": "茶颜悦色",
      "is_certified": 1,
      "credit_score": 100,
      "contact_phone": "138****5678"
    },
    "create_time": "2026-06-29 10:00:00"
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440006"
}
```

#### POST /v1/student/resume - 创建/更新简历

**请求参数**:
```json
{
  "real_name": "张三",
  "gender": 1,
  "available_time": {
    "mon": ["14:00-18:00"],
    "tue": ["14:00-18:00"],
    "wed": [],
    "thu": ["14:00-18:00"],
    "fri": ["14:00-18:00"],
    "sat": ["09:00-18:00"],
    "sun": ["09:00-18:00"]
  },
  "skill_tags": ["沟通能力", "服务意识", "团队协作"],
  "resume_status": 1
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "保存成功",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440007"
}
```

#### GET /v1/student/resume - 查看简历

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "real_name": "张三",
    "gender": 1,
    "school_name": "长沙理工大学",
    "student_no": "2023001234",
    "available_time": {
      "mon": ["14:00-18:00"],
      "tue": ["14:00-18:00"],
      "wed": [],
      "thu": ["14:00-18:00"],
      "fri": ["14:00-18:00"],
      "sat": ["09:00-18:00"],
      "sun": ["09:00-18:00"]
    },
    "skill_tags": ["沟通能力", "服务意识", "团队协作"],
    "resume_status": 1,
    "create_time": "2026-06-28 15:00:00"
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440008"
}
```

#### POST /v1/student/apply - 投递岗位

**请求参数**:
```json
{
  "job_id": "job-abc123"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "投递成功",
  "data": {
    "apply_id": "apply-xyz789",
    "apply_status": 0
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440009"
}
```

**失败响应（重复投递）**:
```json
{
  "code": 409,
  "message": "您已投递该岗位",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440010"
}
```

#### GET /v1/student/apply/list - 投递记录

**请求参数**:
```
?status=0&page=1&size=10
```

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "apply_id": "apply-xyz789",
        "job_id": "job-abc123",
        "job_title": "奶茶店店员",
        "salary_amount": 18.00,
        "enterprise_name": "茶颜悦色",
        "apply_status": 0,
        "apply_status_text": "已投递",
        "create_time": "2026-06-29 11:00:00"
      }
    ],
    "total": 5
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440011"
}
```

#### GET /v1/student/im/messages - IM消息记录

**请求参数**:
```
?target_id=enterprise-123&page=1&size=20
```

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "message_id": "msg-111",
        "from_id": "student-8a2b3c",
        "to_id": "enterprise-123",
        "content": "您好，请问这个岗位还招人吗？",
        "message_type": "text",
        "timestamp": 1700000000000,
        "is_read": 1
      },
      {
        "message_id": "msg-112",
        "from_id": "enterprise-123",
        "to_id": "student-8a2b3c",
        "content": "招的，欢迎投递！",
        "message_type": "text",
        "timestamp": 1700000060000,
        "is_read": 0
      }
    ],
    "total": 20
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440012"
}
```

#### POST /v1/student/im/send - 发送IM消息

**请求参数**:
```json
{
  "target_id": "enterprise-123",
  "content": "请问工作时间是怎样安排的？",
  "message_type": "text"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "message_id": "msg-113"
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440013"
}
```

#### POST /v1/student/agreement/sign - 签署电子协议

**请求参数**:
```json
{
  "job_id": "job-abc123",
  "sign_image": "data:image/png;base64,iVBORw0KGgo...",
  "is_agree": true
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "签署成功",
  "data": {
    "agreement_id": "agreement-456",
    "sign_status": 2,
    "blockchain_hash": "0xabc123..."
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440014"
}
```

#### GET /v1/student/agreement/list - 协议列表

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "agreement_id": "agreement-456",
        "job_id": "job-abc123",
        "job_title": "奶茶店店员",
        "enterprise_name": "茶颜悦色",
        "sign_status": 2,
        "sign_status_text": "已签署",
        "sign_time": "2026-06-29 14:00:00",
        "agreement_url": "https://oss.parttime-cs.com/agreement/agreement-456.pdf"
      }
    ],
    "total": 2
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440015"
}
```

#### POST /v1/student/clock - GPS打卡

**请求参数**:
```json
{
  "job_id": "job-abc123",
  "clock_type": 1,
  "longitude": 112.9399,
  "latitude": 28.2280
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "签到成功",
  "data": {
    "record_id": "clock-789",
    "clock_type": 1,
    "is_abnormal": 0
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440016"
}
```

**异常响应（距离过远）**:
```json
{
  "code": 200,
  "message": "签到成功（位置较远，已标记异常）",
  "data": {
    "record_id": "clock-789",
    "clock_type": 1,
    "is_abnormal": 1,
    "distance": 650
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440017"
}
```

#### GET /v1/student/clock/records - 打卡记录

**请求参数**:
```
?job_id=job-abc123&date=2026-06-29&page=1&size=10
```

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "record_id": "clock-789",
        "job_id": "job-abc123",
        "job_title": "奶茶店店员",
        "clock_type": 1,
        "clock_type_text": "签到",
        "clock_time": "2026-06-29 09:00:00",
        "is_abnormal": 0
      }
    ],
    "total": 10
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440018"
}
```

#### POST /v1/student/clock/appeal - 打卡异常申诉

**请求参数**:
```json
{
  "record_id": "clock-789",
  "appeal_reason": "当时手机定位偏差，实际已到岗",
  "evidence_urls": ["https://oss.parttime-cs.com/evidence/1.jpg"]
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "申诉已提交",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440019"
}
```

#### GET /v1/student/salary/flow - 薪资流水

**请求参数**:
```
?month=2026-06&page=1&size=10
```

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "overview": {
      "month_income": 720.00,
      "total_income": 1500.00,
      "pending_amount": 0.00
    },
    "list": [
      {
        "flow_id": "flow-001",
        "enterprise_name": "茶颜悦色",
        "job_title": "奶茶店店员",
        "work_hours": 40.00,
        "hourly_wage": 18.00,
        "salary_amount": 720.00,
        "tax_amount": 0.00,
        "actual_amount": 720.00,
        "settlement_status": 3,
        "settlement_status_text": "已到账",
        "pay_time": "2026-06-29 18:00:00"
      }
    ],
    "total": 3
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440020"
}
```

#### POST /v1/student/complaint - 发起投诉

**请求参数**:
```json
{
  "defendant_id": "enterprise-123",
  "defendant_type": 2,
  "job_id": "job-abc123",
  "complaint_type": 1,
  "complaint_content": "岗位描述与实际工作内容不符，实际工作时间远超描述",
  "evidence_urls": ["https://oss.parttime-cs.com/evidence/1.jpg", "https://oss.parttime-cs.com/evidence/2.jpg"]
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "投诉已提交",
  "data": {
    "complaint_id": "complaint-001",
    "status": 0
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440021"
}
```

#### GET /v1/student/complaint/list - 投诉记录

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "complaint_id": "complaint-001",
        "complaint_type": 1,
        "complaint_type_text": "虚假招聘",
        "defendant_name": "茶颜悦色",
        "complaint_content": "岗位描述与实际工作内容不符",
        "status": 0,
        "status_text": "待审核",
        "create_time": "2026-06-29 15:00:00"
      }
    ],
    "total": 1
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440022"
}
```

#### PUT /v1/student/profile - 修改个人信息

**请求参数**:
```json
{
  "avatar_url": "https://oss.parttime-cs.com/avatar/1.jpg",
  "nickname": "小张",
  "gender": 1
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "修改成功",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440023"
}
```

#### POST /v1/student/feedback - 评价企业/岗位

**请求参数**:
```json
{
  "job_id": "job-abc123",
  "rating": 5,
  "comment": "工作环境好，薪资发放及时，推荐！",
  "work_content": "奶茶制作、收银"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "评价成功",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440024"
}
```

### 4.4 企业端接口

#### POST /v1/enterprise/auth/register - 注册+提交资质

**请求参数**:
```json
{
  "enterprise_name": "长沙茶颜悦色餐饮管理有限公司",
  "credit_code": "91430100MA4L16H8XX",
  "business_license_url": "https://oss.parttime-cs.com/license/1.jpg",
  "legal_person": "吕良",
  "contact_name": "王经理",
  "contact_phone": "139****5678",
  "industry_tag": "茶饮"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "注册成功，待审核",
  "data": {
    "enterprise_id": "enterprise-123",
    "verify_status": 1
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440025"
}
```

#### GET /v1/enterprise/auth/status - 查看认证状态

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "enterprise_id": "enterprise-123",
    "enterprise_name": "长沙茶颜悦色餐饮管理有限公司",
    "verify_status": 2,
    "verify_status_text": "已认证",
    "credit_score": 100,
    "audit_opinion": "企业资质真实有效"
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440026"
}
```

#### POST /v1/enterprise/job/publish - 发布岗位

**请求参数**:
```json
{
  "job_title": "奶茶店店员",
  "job_type": "兼职",
  "industry_tag": "茶饮",
  "salary_type": 1,
  "salary_amount": 18.00,
  "settlement_type": 1,
  "work_address": "长沙市岳麓区麓山南路88号",
  "work_time": {
    "sat": ["09:00-18:00"],
    "sun": ["09:00-18:00"]
  },
  "skill_require": "沟通能力强、有服务意识",
  "job_desc": "负责奶茶制作、收银、门店卫生等工作",
  "recruit_num": 5,
  "is_insured": 1
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "job_id": "job-abc123",
    "status": 1
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440027"
}
```

**失败响应（薪资低于最低标准）**:
```json
{
  "code": 422,
  "message": "薪资不得低于长沙最低时薪17元",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440028"
}
```

#### PUT /v1/enterprise/job/update - 编辑岗位

**请求参数**:
```json
{
  "job_id": "job-abc123",
  "job_title": "奶茶店店员（周末兼职）",
  "salary_amount": 19.00
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440029"
}
```

#### PUT /v1/enterprise/job/offline - 下架岗位

**请求参数**:
```json
{
  "job_id": "job-abc123"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "下架成功",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440030"
}
```

#### GET /v1/enterprise/applicant/list - 查看投递简历

**请求参数**:
```
?job_id=job-abc123&status=0&page=1&size=10
```

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "apply_id": "apply-xyz789",
        "student_id": "student-8a2b3c",
        "real_name": "张三",
        "school_name": "长沙理工大学",
        "phone": "138****1234",
        "skill_tags": ["沟通能力", "服务意识"],
        "available_time": {
          "sat": ["09:00-18:00"],
          "sun": ["09:00-18:00"]
        },
        "apply_status": 0,
        "apply_status_text": "已投递",
        "apply_time": "2026-06-29 11:00:00"
      }
    ],
    "total": 15
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440031"
}
```

#### PUT /v1/enterprise/applicant/status - 标记候选人状态

**请求参数**:
```json
{
  "apply_id": "apply-xyz789",
  "status": 1
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440032"
}
```

#### POST /v1/enterprise/invite-interview - 邀请面试

**请求参数**:
```json
{
  "apply_id": "apply-xyz789",
  "interview_time": "2026-07-01 14:00:00",
  "interview_type": "video"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "面试邀请已发送",
  "data": {
    "room_id": "room-123456"
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440033"
}
```

#### POST /v1/enterprise/attendance/confirm - 确认工时

**请求参数**:
```json
{
  "student_id": "student-8a2b3c",
  "job_id": "job-abc123",
  "work_date": "2026-06-29",
  "confirmed_hours": 8.00,
  "note": ""
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "工时确认成功",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440034"
}
```

#### POST /v1/enterprise/salary/pay - 发起薪资发放

**请求参数**:
```json
{
  "flow_id": "flow-001"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "发放请求已提交",
  "data": {},
  "trace_id": "550e8400-e29b-41d4-a716-446655440035"
}
```

#### GET /v1/enterprise/salary/records - 薪资发放记录

**请求参数**:
```
?month=2026-06&page=1&size=10
```

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "flow_id": "flow-001",
        "student_name": "张三",
        "job_title": "奶茶店店员",
        "work_hours": 40.00,
        "salary_amount": 720.00,
        "actual_amount": 720.00,
        "settlement_status": 3,
        "settlement_status_text": "已到账",
        "pay_time": "2026-06-29 18:00:00"
      }
    ],
    "total": 10
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440036"
}
```

### 4.5 管理后台接口

#### GET /v1/pc/admin/enterprise/audit/list - 待审核企业列表

**请求参数**:
```
?status=1&page=1&size=20
```

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "enterprise_id": "enterprise-456",
        "enterprise_name": "长沙某餐饮公司",
        "credit_code": "91430100MA4LXXXXX",
        "legal_person": "李四",
        "contact_name": "赵经理",
        "contact_phone": "137****9012",
        "industry_tag": "餐饮",
        "verify_status": 1,
        "create_time": "2026-06-29 09:00:00"
      }
    ],
    "total": 5
  },
  "trace_id": "550e8400-e29b-41d4-a716-446655440037"
}
```

#### POST /v1/pc/admin/enterprise/audit - 企业审核

**请求参数**:
```json
{
  "enterprise_id": "enterprise-456",
  "status": 2,
  "opinion": "企业资质真实有效，审核通过"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "审核成功",
  "data": {},
  "trace_id