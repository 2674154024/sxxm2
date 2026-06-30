# 长沙大学生兼职平台 - 测试用例文档

> **版本**: V1.0 | **日期**: 2026-06-29 | **测试框架**: Postman Collection Runner + JMeter

> **文档偏差说明**: 参考 [DEVIATIONS.md](file:///d:/sxxm2/miniapp/DEVIATIONS.md)，小程序端已从 Vue2+Vuex 变更为 Vue3+Pinia，测试时需注意前端状态管理方式差异。

---

## 目录

1. [功能测试用例](#一功能测试用例)
2. [安全测试用例](#二安全测试用例)
3. [合规测试用例](#三合规测试用例)
4. [小程序端E2E测试用例](#四小程序端e2e测试用例)
5. [JMeter性能测试脚本](#五jmeter性能测试脚本)

---

## 一、功能测试用例

### TC-001: 学生注册登录流程

| 步骤 | 请求 | 预期结果 |
|------|------|---------|
| 1 | POST `/v1/auth/sms-code` <br> `{"phone": "13800138000"}` | 返回200，提示"验证码已发送" |
| 2 | POST `/v1/auth/phone-login` <br> `{"phone": "13800138000", "code": "123456"}` | 返回200，包含token和用户信息 |
| 3 | GET `/v1/student/profile` <br> Header: `Authorization: Bearer {token}` | 返回200，用户信息完整 |
| 验证 | 检查数据库 `t_student` 表 | `student_id` 不为空，`verify_status=0` |

### TC-002: 学生实名认证

| 步骤 | 请求 | 预期结果 |
|------|------|---------|
| 1 | POST `/v1/student/auth/register` <br> Header: `Authorization: Bearer {token}` <br> `{"real_name": "张三", "student_no": "20230001", "school_id": 1, "id_card": "430000199001011234", "phone": "13800138000"}` | 返回200，提示"实名认证提交成功" |
| 2 | 检查数据库 `t_student` 表 | `id_card_encrypt` 和 `phone_encrypt` 为AES加密密文（非明文） |
| 3 | GET `/v1/student/profile` | API返回的手机号中间4位为 `****`，身份证中间8位为 `********` |
| 验证 | Mock学信网接口返回成功 | `verify_status` 变为2（已认证） |

### TC-003: 企业注册认证流程

| 步骤 | 请求 | 预期结果 |
|------|------|---------|
| 1 | POST `/v1/enterprise/auth/register` <br> `{"enterprise_name": "长沙科技有限公司", "credit_code": "91430100MA4L123456", "business_license": "base64图片内容", "legal_person": "李四", "contact_name": "王五", "contact_phone": "13900139000"}` | 返回200，生成 `enterprise_id` |
| 2 | 检查数据库 `t_enterprise` 表 | `verify_status=1`（审核中），`contact_phone_encrypt` 为密文 |
| 3 | POST `/v1/pc/admin/enterprise/audit` <br> Header: 审核管理员token <br> `{"enterprise_id": "{enterprise_id}", "action": "pass", "reason": "审核通过"}` | 返回200 |
| 4 | 检查数据库 | `verify_status=2`（已认证），`is_certified=1` |

### TC-004: 岗位发布+审核流程

| 步骤 | 请求 | 预期结果 |
|------|------|---------|
| 1 | POST `/v1/enterprise/job/publish` <br> Header: 认证企业token <br> `{"job_title": "奶茶店店员", "job_type": 1, "industry_tag": "茶饮", "salary_type": 1, "salary_amount": 18, "settlement_type": 1, "work_address": "长沙市雨花区", "work_time": "[{\"day\":\"mon\",\"time\":\"10:00-18:00\"}]", "skill_require": "押金100元", "recruit_num": 5}` | 返回200 |
| 2 | 检查数据库 `t_job` 表 | `status=0`（待审核），因含敏感词"押金" |
| 3 | GET `/v1/public/job/list` | 该岗位不在公开列表显示 |
| 4 | POST `/v1/enterprise/job/publish` <br> Header: 认证企业token <br> `{"job_title": "超市导购", "job_type": 1, "industry_tag": "零售", "salary_type": 1, "salary_amount": 17, "settlement_type": 1, "work_address": "长沙市天心区", "work_time": "[{\"day\":\"mon\",\"time\":\"09:00-17:00\"}]", "skill_require": "沟通能力", "recruit_num": 3}` | 返回200 |
| 5 | 检查数据库 | `status=1`（已发布），不含敏感词且时薪≥17 |
| 6 | GET `/v1/public/job/list` | 可查询到该岗位 |

### TC-005: 岗位搜索（LBS）

| 步骤 | 请求 | 预期结果 |
|------|------|---------|
| 1 | GET `/v1/student/job/list?longitude=112.9388&latitude=28.2282&distance=5&page=1&size=20` | 返回200，岗位列表按距离降序排列 |
| 2 | GET `/v1/student/job/list?longitude=112.9388&latitude=28.2282&salary_min=20&industry_tag=茶饮` | 返回200，筛选时薪≥20且行业为茶饮的岗位 |
| 3 | GET `/v1/student/job/detail?job_id={job_id}` | 返回200，岗位详情包含企业信用分、认证标识、意外险信息 |

### TC-006: 智能匹配算法（Match-Service）

| 步骤 | 请求 | 预期结果 |
|------|------|---------|
| 1 | POST `/v1/match/calculate` <br> Header: 学生token <br> `{"student_id": "{student_id}", "job_id": "{job_id}"}` | 返回200，匹配分数0-100 |
| 2 | 验证算法逻辑 | 综合评分 = 技能匹配度×40% + 时间可用性×30% + 通勤距离×30% |
| 3 | POST `/v1/match/recommend` <br> Header: 学生token <br> `{}` | 返回200，推荐Top20岗位 |
| 4 | 验证推荐结果 | 按匹配分降序排列，记录 `t_job_match_log` |
| 5 | POST `/v1/enterprise/match-candidates` <br> Header: 企业token <br> `{"job_id": "{job_id}"}` | 返回200，推荐Top20候选人 |

### TC-007: 投递+签约+打卡+结算全流程

| 步骤 | 请求 | 预期结果 |
|------|------|---------|
| 1 | POST `/v1/student/apply` <br> Header: 学生token <br> `{"job_id": "{job_id}"}` | 返回200，`apply_status=0`（已投递） |
| 2 | POST `/v1/enterprise/apply/audit` <br> Header: 企业token <br> `{"apply_id": "{apply_id}", "action": "interview"}` | 返回200，`apply_status=1`（待面试） |
| 3 | POST `/v1/enterprise/apply/audit` <br> Header: 企业token <br> `{"apply_id": "{apply_id}", "action": "hire"}` | 返回200，`apply_status=2`（已录用） |
| 4 | POST `/v1/student/agreement/sign` <br> Header: 学生token <br> `{"apply_id": "{apply_id}", "signature": "base64签名", "face_image": "base64人脸图片"}` | 返回200，`sign_status=2`（已签署） |
| 5 | POST `/v1/student/clock` <br> Header: 学生token <br> `{"job_id": "{job_id}", "clock_type": 1, "longitude": 112.9388, "latitude": 28.2282}` | 返回200，签到成功，`is_abnormal=0` |
| 6 | POST `/v1/student/clock` <br> Header: 学生token <br> `{"job_id": "{job_id}", "clock_type": 2, "longitude": 112.9388, "latitude": 28.2282}` | 返回200，签退成功，自动计算工时 |
| 7 | POST `/v1/enterprise/attendance/confirm` <br> Header: 企业token <br> `{"student_id": "{student_id}", "job_id": "{job_id}", "work_date": "2026-06-29", "confirmed_hours": 8}` | 返回200，`settlement_status=1`（待确认薪资） |
| 8 | POST `/v1/enterprise/salary/calculate` <br> Header: 企业token <br> `{"job_id": "{job_id}", "month": "2026-06"}` | 返回200，生成薪资流水 |
| 9 | POST `/v1/enterprise/salary/pay` <br> Header: 企业token <br> `{"flow_id": "{flow_id}"}` | 返回200，薪资发放成功 |
| 10 | GET `/v1/student/salary/flow` <br> Header: 学生token | 返回200，显示已到账记录，`settlement_status=3` |

### TC-008: 薪资异常场景

| 场景 | 请求 | 预期结果 |
|------|------|---------|
| 余额不足 | POST `/v1/enterprise/salary/pay` <br> Header: 企业token（托管余额<应发金额） | 返回422，提示"托管账户余额不足" |
| 重复发放 | 同一 `flow_id` 再次调用发放接口 | 返回409（幂等性验证），提示"该流水已处理" |
| 支付回调丢失 | 模拟支付成功但回调未到达 | 定时任务查询支付结果并补单，最终 `settlement_status=3` |

### TC-009: 投诉处理流程

| 步骤 | 请求 | 预期结果 |
|------|------|---------|
| 1 | POST `/v1/student/complaint` <br> Header: 学生token <br> `{"defendant_id": "{enterprise_id}", "defendant_type": "enterprise", "job_id": "{job_id}", "complaint_type": "salary_delay", "complaint_content": "企业拖欠薪资", "evidence_urls": ["https://xxx.com/evidence.jpg"]}` | 返回200，`status=0`（待审核） |
| 2 | POST `/v1/pc/admin/risk/complaint/handle` <br> Header: 风控管理员token <br> `{"complaint_id": "{complaint_id}", "action": "freeze", "amount": 1000}` | 返回200，企业托管账户冻结1000元 |
| 3 | POST `/v1/pc/admin/risk/complaint/handle` <br> Header: 风控管理员token <br> `{"complaint_id": "{complaint_id}", "action": "deduct", "amount": 1000, "handle_result": "投诉成立，已划扣补偿"}` | 返回200，划扣金额给投诉学生，`status=2`（已处理） |

### TC-010: GPS打卡异常场景

| 场景 | 请求 | 预期结果 |
|------|------|---------|
| 距离超500m | POST `/v1/student/clock` <br> `{"job_id": "{job_id}", "clock_type": 1, "longitude": 113.0, "latitude": 28.3}`（距离岗位>500m） | 返回200，`is_abnormal=1`（异常打卡） |
| 重复签到 | 同一天同一岗位再次签到 | 返回409，提示"今日已签到" |
| 重复签退 | 同一天同一岗位再次签退 | 返回409，提示"今日已签退" |
| 异常申诉 | POST `/v1/student/clock/appeal` <br> `{"record_id": "{record_id}", "appeal_reason": "临时有事外出", "evidence_urls": ["https://xxx.com/evidence.jpg"]}` | 返回200，申诉提交成功 |

### TC-011: IM聊天功能

| 步骤 | 请求 | 预期结果 |
|------|------|---------|
| 1 | WebSocket连接 `wss://api.parttime-cs.com/ws/im?token={token}` | 连接成功，建立会话 |
| 2 | POST `/v1/student/im/send` <br> `{"target_id": "{enterprise_id}", "content": "您好，我想了解岗位详情", "message_type": "text"}` | 返回200，消息发送成功 |
| 3 | GET `/v1/student/im/messages?target_id={enterprise_id}&page=1&size=20` | 返回200，历史消息列表 |
| 4 | GET `/v1/student/im/conversation-list` | 返回200，会话列表包含最后一条消息和未读数 |
| 5 | 模拟对方离线发送消息 | 消息存入Redis未读队列，对方上线后自动推送 |
| 6 | WebSocket断线重连测试 | 客户端自动重连，保留最近50条未读消息 |
| 7 | 心跳检测测试 | 每30秒ping/pong，超时3次断开连接 |

### TC-012: 视频面试邀请

| 步骤 | 请求 | 预期结果 |
|------|------|---------|
| 1 | POST `/v1/enterprise/invite-interview` <br> Header: 企业token <br> `{"apply_id": "{apply_id}", "interview_time": "2026-06-30 14:00:00"}` | 返回200，生成TRTC房间号 |
| 2 | 检查数据库 `t_job_apply` 表 | `apply_status=1`（待面试），`interview_time` 已设置 |
| 3 | 检查通知服务 | 学生收到面试提醒（IM+短信） |

### TC-013: 通知服务（Notification-Service）

| 场景 | 请求 | 预期结果 |
|------|------|---------|
| 短信通知发送 | 触发面试提醒场景 | 学生收到短信："【长沙兼职平台】{企业名}邀请您于{时间}参加面试" |
| 薪资到账通知 | 薪资发放成功后 | 学生收到短信："【长沙兼职平台】您的兼职薪资{金额}元已到账" |
| 审核结果通知 | 岗位审核完成后 | 企业收到短信："【长沙兼职平台】您的{岗位}审核已{通过/未通过}" |
| 站内信查询 | GET `/v1/notification/list` <br> Header: 用户token | 返回200，站内信列表 |
| 站内信已读 | PUT `/v1/notification/read/{id}` <br> Header: 用户token | 返回200，标记为已读 |

### TC-014: 企业端功能测试

| 场景 | 请求 | 预期结果 |
|------|------|---------|
| 企业数据看板 | GET `/v1/pc/enterprise/stat/dashboard` <br> Header: 企业token | 返回200，包含岗位数、投递数、录用数、薪资总额 |
| 人才库查询 | GET `/v1/pc/enterprise/talent/list?skill_tags=编程&credit_min=80` <br> Header: 企业token | 返回200，筛选后的学生列表 |
| 岗位批量导入 | POST `/v1/pc/enterprise/job/batch-publish` <br> Header: 企业token（上传Excel文件） | 返回200，导入成功X条，失败X条 |
| 智能排班 | POST `/v1/pc/enterprise/schedule/save` <br> Header: 企业token | 返回200，排班保存成功 |

### TC-015: 数据服务（Data-Service）

| 场景 | 请求 | 预期结果 |
|------|------|---------|
| 运营数据报表 | GET `/v1/pc/admin/operation/report?start_date=2026-06-01&end_date=2026-06-30` <br> Header: 运营管理员token | 返回200，用户增长、岗位发布量、投递量、投诉量 |
| 财务报表 | GET `/v1/pc/admin/finance/report?month=2026-06` <br> Header: 财务管理员token | 返回200，收入趋势、支出构成、毛利分析 |
| 数据导出 | GET `/v1/pc/admin/data/export?type=user_list` <br> Header: 超级管理员token | 返回Excel文件下载 |

### TC-016: 边界值测试

| 场景 | 请求 | 预期结果 |
|------|------|---------|
| 时薪边界值=17 | POST `/v1/enterprise/job/publish` <br> `{"salary_amount": 17}` | 返回200，发布成功（刚好达标） |
| 时薪边界值=16.99 | POST `/v1/enterprise/job/publish` <br> `{"salary_amount": 16.99}` | 返回422，提示"不低于长沙最低时薪" |
| 信用分上限=200 | 学生信用分=195，完成履约+5 | 信用分保持200（不超过上限） |
| 信用分下限=0 | 学生信用分=15，被投诉-20 | 信用分保持0（不低于下限） |
| 打卡距离=500m | POST `/v1/student/clock` <br> 距离岗位=500m | 返回200，`is_abnormal=0`（正常） |
| 打卡距离=501m | POST `/v1/student/clock` <br> 距离岗位=501m | 返回200，`is_abnormal=1`（异常） |

### TC-017: 空数据状态测试

| 场景 | 请求 | 预期结果 |
|------|------|---------|
| 无附近岗位 | GET `/v1/student/job/list?longitude=0&latitude=0&distance=5` | 返回200，`list`为空数组，`total=0` |
| 无投递记录 | GET `/v1/student/apply/list` <br> Header: 新注册学生token | 返回200，`list`为空数组 |
| 无打卡记录 | GET `/v1/student/clock/records` <br> Header: 未入职学生token | 返回200，`list`为空数组 |
| 无会话消息 | GET `/v1/student/im/conversation-list` <br> Header: 新注册学生token | 返回200，`list`为空数组 |

### TC-018: 信用评分定时任务

| 场景 | 验证 | 预期结果 |
|------|------|---------|
| 学生履约加分 | 学生完成1次准时打卡 | 信用分+5（范围0-200） |
| 学生投诉减分 | 学生被有效投诉1次 | 信用分-20（范围0-200） |
| 企业准时发放加分 | 企业准时发放薪资1次 | 信用分+5（范围0-200） |
| 企业投诉减分 | 企业被有效投诉1次 | 信用分-20（范围0-200） |
| 定时执行 | 每日凌晨2点检查定时任务日志 | 任务执行成功 |

---

## 二、安全测试用例

### TC-SEC-001: SQL注入测试

| 测试点 | 请求 | 预期结果 |
|--------|------|---------|
| GET参数注入 | GET `/v1/public/job/list?keyword=' OR '1'='1` | 返回400或正常处理，不执行注入SQL |
| POST body注入 | POST `/v1/enterprise/job/publish` <br> `{"job_title": "'; DROP TABLE t_job; --"}` | 返回400，请求参数校验失败 |
| 搜索接口注入 | GET `/v1/student/job/list?keyword=奶茶' OR 1=1--` | 返回400或正常搜索结果 |
| 排序参数注入 | GET `/v1/public/job/list?sort=salary_amount'; DROP TABLE` | 返回400，参数格式校验失败 |

### TC-SEC-002: XSS攻击测试

| 测试点 | 请求 | 预期结果 |
|--------|------|---------|
| 岗位描述XSS | POST `/v1/enterprise/job/publish` <br> `{"skill_require": "<script>alert('xss')</script>"}` | 返回400或HTML标签被转义 |
| 简历内容XSS | POST `/v1/student/resume` <br> `{"skill_tags": ["<img src=x onerror=alert(1)>"]}` | 返回400或HTML标签被过滤 |
| 投诉内容XSS | POST `/v1/student/complaint` <br> `{"complaint_content": "<iframe src=javascript:alert(1)>"}` | 返回400或HTML标签被转义 |
| 企业名称XSS | POST `/v1/enterprise/auth/register` <br> `{"enterprise_name": "<script>alert('xss')</script>"}` | 返回400或HTML标签被过滤 |

### TC-SEC-003: 未授权访问测试

| 测试场景 | 请求 | 预期结果 |
|----------|------|---------|
| 学生调用企业接口 | POST `/v1/enterprise/job/publish` <br> Header: 学生token | 返回403，无权限 |
| 企业调用管理接口 | POST `/v1/pc/admin/enterprise/audit` <br> Header: 企业token | 返回403，无权限 |
| 无token调用需认证接口 | POST `/v1/student/apply` <br> 无Authorization头 | 返回401，未授权 |
| 风控管理员调用财务接口 | POST `/v1/pc/admin/finance/settlement/pay` <br> Header: 风控管理员token | 返回403，无权限 |
| 审核管理员调用风控接口 | POST `/v1/pc/admin/risk/complaint/handle` <br> Header: 审核管理员token | 返回403，无权限 |

### TC-SEC-004: JWT伪造测试

| 测试场景 | 请求 | 预期结果 |
|----------|------|---------|
| 修改JWT中role字段 | 使用伪造token（role从student改为admin）调用管理接口 | 返回401，token无效 |
| 使用过期token | 使用过期JWT调用任意接口 | 返回401，token已过期 |
| 修改JWT中user_id | 修改JWT中的user_id为其他用户ID | 返回401，签名验证失败 |
| 无签名token | 使用无签名的JWT调用接口 | 返回401，token无效 |
| 错误签名token | 使用错误密钥签名的JWT调用接口 | 返回401，签名验证失败 |

### TC-SEC-005: 接口限流测试

| 测试场景 | 请求 | 预期结果 |
|----------|------|---------|
| 公开接口限流 | 快速连续请求 `/v1/public/job/list` 15次/分钟 | 超过10次后返回429 Too Many Requests |
| 核心业务接口限流 | 快速连续请求 `/v1/student/apply` 8次/分钟 | 超过5次后返回429 Too Many Requests |
| 管理后台限流 | 快速连续请求 `/v1/pc/admin/enterprise/audit/list` 25次/分钟 | 超过20次后返回429 Too Many Requests |
| IP限流 | 同一IP快速请求公开接口100次/分钟 | 超过限流阈值后返回429 |

### TC-SEC-006: 敏感信息泄露测试

| 测试场景 | 请求 | 预期结果 |
|----------|------|---------|
| 数据库错误信息泄露 | 故意触发数据库查询异常 | 返回500，不暴露堆栈信息 |
| 日志敏感信息打印 | 检查应用日志 | 不打印身份证、手机号、密码明文 |
| GET请求传递敏感参数 | GET `/v1/student/profile?id_card=430000199001011234` | 返回400或忽略敏感参数 |
| 接口响应敏感字段 | GET `/v1/student/profile` | 身份证中间8位脱敏，手机号中间4位脱敏 |
| Redis缓存敏感信息 | 检查Redis缓存数据 | 敏感字段为加密后的值 |

### TC-SEC-007: 越权访问测试

| 测试场景 | 请求 | 预期结果 |
|----------|------|---------|
| 学生查看他人简历 | GET `/v1/student/resume` <br> Header: 学生A的token（尝试查看学生B的简历） | 返回403，只能查看自己的简历 |
| 企业查看其他企业岗位 | GET `/v1/enterprise/job/my-list` <br> Header: 企业A的token | 返回403或只显示企业A的岗位 |
| 学生查看其他学生薪资 | GET `/v1/student/salary/flow` <br> Header: 学生A的token（尝试查看学生B的薪资） | 返回403，只能查看自己的薪资 |
| 管理员查看其他管理员审计日志 | GET `/v1/pc/admin/audit-log/list` <br> Header: 普通管理员token | 返回403，超级管理员权限 |

### TC-SEC-008: 文件上传安全测试

| 测试场景 | 请求 | 预期结果 |
|----------|------|---------|
| 上传恶意文件 | POST `/v1/enterprise/auth/register` <br> `{"business_license": "base64编码的恶意脚本"}` | 返回400，文件类型校验失败 |
| 上传超大文件 | POST `/v1/student/complaint` <br> 上传超过10MB的图片 | 返回400，文件大小超出限制 |
| 上传无扩展名文件 | POST `/v1/enterprise/auth/register` <br> 上传无扩展名文件 | 返回400，文件格式不合法 |
| 上传伪装文件 | 上传扩展名为.jpg但内容为PHP脚本的文件 | 返回400，文件内容校验失败 |

### TC-SEC-009: CSRF攻击测试

| 测试场景 | 请求 | 预期结果 |
|----------|------|---------|
| CSRF Token验证 | 不带CSRF Token提交表单 | 返回400或403，请求被拒绝 |
| 无效CSRF Token | 使用无效CSRF Token提交表单 | 返回400或403，Token验证失败 |

---

## 三、合规测试用例

### TC-CPL-001: 敏感数据加密验证

| 验证项 | 操作 | 预期结果 |
|--------|------|---------|
| 身份证加密存储 | 查询数据库 `t_student` 表的 `id_card_encrypt` 字段 | 值为AES加密密文，非明文 |
| 手机号加密存储 | 查询数据库 `t_student` 表的 `phone_encrypt` 字段 | 值为AES加密密文，非明文 |
| 企业联系人电话加密 | 查询数据库 `t_enterprise` 表的 `contact_phone_encrypt` 字段 | 值为AES加密密文，非明文 |
| API返回手机号脱敏 | GET `/v1/student/profile` | 返回格式为 `138****8000` |
| API返回身份证脱敏 | GET `/v1/student/profile` | 返回格式为 `430000********1234` |
| AES密钥来源 | 检查应用配置 | 从环境变量 `AES_SECRET_KEY` 读取，不硬编码 |

### TC-CPL-002: 审计日志完整性

| 验证项 | 操作 | 预期结果 |
|--------|------|---------|
| 审核操作审计 | 执行企业审核操作后查询 `t_audit_log` | 记录存在，包含 `operator_id`、`module`、`action` |
| 结算操作审计 | 执行薪资发放操作后查询 `t_audit_log` | 记录存在，包含 `request_params`（脱敏）、`ip_address` |
| 投诉处理审计 | 执行投诉处理操作后查询 `t_audit_log` | 记录存在，包含 `client_type`、`trace_id` |
| 岗位发布审计 | 执行岗位发布操作后查询 `t_audit_log` | 记录存在，参数脱敏 |
| 异步写入验证 | 执行操作后立即查询 `t_audit_log` | 记录可能延迟写入（异步），但最终会写入 |

### TC-CPL-003: 五流合一验证

| 验证项 | 操作 | 预期结果 |
|--------|------|---------|
| 合同流关联 | 查询 `t_salary_flow` 表 | `agreement_id` 字段存在且关联有效合同 |
| 资金流关联 | 查询 `t_salary_flow` 表 | `salary_flow_id` 唯一标识该笔资金流水 |
| 发票流关联 | 查询 `t_salary_flow` 表 | `invoice_id` 字段存在（如已开具发票） |
| 业务流关联 | 查询 `t_salary_flow` 表 | `job_id` 字段关联对应的岗位 |
| 数据流关联 | 查询 `t_salary_flow` 表 | `trace_id` 字段存在，可追溯请求链路 |
| 五流关联一致性 | 验证同一笔薪资流水的五个字段 | 均指向同一业务场景，关联关系正确 |

### TC-CPL-004: HTTPS验证

| 验证项 | 操作 | 预期结果 |
|--------|------|---------|
| HTTP自动跳转HTTPS | 使用HTTP协议访问API | 自动跳转到HTTPS |
| HTTPS强制验证 | 验证Nginx配置 | HTTP请求返回301重定向到HTTPS |
| TLS版本验证 | 使用SSL检测工具 | TLS版本≥1.2 |
| 证书有效性验证 | 检查SSL证书 | 证书有效且未过期 |

### TC-CPL-005: 金额计算规范验证

| 验证项 | 操作 | 预期结果 |
|--------|------|---------|
| BigDecimal使用 | 检查后端代码 | 金额计算使用BigDecimal，禁止float/double |
| 时薪最低标准 | 发布时薪<17的岗位 | 返回422，提示"不低于长沙最低时薪" |
| 薪资计算精度 | 计算8.5小时×17元/小时 | 结果为144.5元（精确到0.5小时） |
| 个税计算 | 计算累计收入50000元的个税 | 使用累计预扣法，结果正确 |

### TC-CPL-006: 敏感词过滤验证

| 验证项 | 操作 | 预期结果 |
|--------|------|---------|
| 押金敏感词检测 | 发布含"押金"的岗位 | 自动标记 `status=0`（待审核） |
| 培训费敏感词检测 | 发布含"培训费"的岗位 | 自动标记 `status=0`（待审核） |
| 中介费敏感词检测 | 发布含"中介费"的岗位 | 自动标记 `status=0`（待审核） |
| 高薪零门槛检测 | 发布含"高薪零门槛"的岗位 | 自动标记 `status=0`（待审核） |
| 正常岗位通过 | 发布不含敏感词的正常岗位 | 自动标记 `status=1`（已发布） |
| 敏感词库加载 | 检查应用启动日志 | 敏感词库加载成功 |

### TC-CPL-007: 数据留存合规

| 验证项 | 操作 | 预期结果 |
|--------|------|---------|
| IM消息留存 | 查询 `t_im_message` 表 | 消息不可删除，留存3年 |
| 审计日志留存 | 查询 `t_audit_log` 表 | 日志永久留存 |
| 薪资流水留存 | 查询 `t_salary_flow` 表 | 流水永久留存 |
| 备份机制 | 检查备份脚本 `backup.sh` | 每日全量备份，每小时增量备份 |
| 备份保留期 | 检查备份配置 | 保留最近30天备份 |

### TC-CPL-008: 隐私合规验证

| 验证项 | 操作 | 预期结果 |
|--------|------|---------|
| 用户数据删除 | 用户发起注销请求 | 15天内完成数据删除，相关数据匿名化处理 |
| 数据导出 | 用户发起数据导出请求 | 返回完整的个人数据副本 |
| 隐私政策展示 | 检查小程序端隐私政策入口 | 用户首次登录时展示隐私政策并需同意 |
| Cookie政策 | 检查前端Cookie使用 | 仅存储必要的session信息，无过度追踪 |

---

## 四、小程序端E2E测试用例

> **注意**: 小程序端已从 Vue2+Vuex 变更为 Vue3+Pinia，测试时需关注前端状态管理方式差异。

### TC-E2E-001: 首页浏览流程

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 打开小程序首页 | 显示安全警示滚动条、搜索栏、分类标签、LBS推荐区 |
| 2 | 点击分类标签"茶饮" | 筛选出茶饮行业的岗位列表 |
| 3 | 点击"查看更多" | 跳转岗位列表页，显示更多岗位 |
| 4 | 下拉刷新 | 刷新岗位列表，加载最新数据 |
| 5 | 上滑加载更多 | 加载下一页岗位数据 |

### TC-E2E-002: 岗位详情+投递流程

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 点击岗位卡片进入详情页 | 显示岗位信息、企业信息、安全提示 |
| 2 | 点击"收藏"按钮 | 收藏成功，图标变为黄色 |
| 3 | 点击"立即投递"按钮（未登录） | 弹出登录引导弹窗 |
| 4 | 登录后点击"立即投递"（未实名） | 弹出实名认证引导弹窗 |
| 5 | 实名认证后点击"立即投递" | 弹出确认弹窗："已知晓岗位要求，确认岗位无押金/培训费等收费项目" |
| 6 | 勾选确认并提交 | 投递成功动效，按钮变为"已投递" |

### TC-E2E-003: 简历管理流程

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 进入个人中心点击"我的简历" | 显示简历编辑页 |
| 2 | 设置可工作时间（周一至周五上午） | 时间选择器正常工作 |
| 3 | 选择技能标签（编程、英语） | 标签选中状态正确 |
| 4 | 点击"保存" | 提示"简历保存成功" |
| 5 | 点击"预览" | 以卡片形式展示完整简历 |

### TC-E2E-004: GPS打卡流程

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 进入打卡页 | 显示当前状态、签到/签退按钮、当前位置 |
| 2 | 点击"签到"按钮 | 获取GPS坐标，显示签到成功动效 |
| 3 | 点击"签退"按钮 | 获取GPS坐标，显示签退成功动效 |
| 4 | 查看打卡记录列表 | 显示历史打卡记录，正常打卡绿色圆点 |

### TC-E2E-005: 薪资流水页

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 进入薪资流水页 | 显示顶部收入总览卡片（本月收入、累计收入、待结算金额） |
| 2 | 滑动月份选择器 | 切换月份，流水列表更新 |
| 3 | 点击流水卡片 | 进入薪资明细页，显示应发、个税、实发金额 |

### TC-E2E-006: 安全中心+投诉流程

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 进入安全中心 | 显示安全警示滚动区、防骗指南FAQ |
| 2 | 点击"一键举报" | 跳转投诉页面 |
| 3 | 选择投诉类型"虚假招聘" | 投诉类型选中 |
| 4 | 填写投诉内容（≥20字） | 内容输入正常 |
| 5 | 上传证据图片（最多9张） | 图片上传成功 |
| 6 | 点击"提交" | 提示"投诉已提交，平台将在24小时内处理" |

### TC-E2E-007: IM聊天流程

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 进入消息Tab | 显示会话列表 |
| 2 | 点击会话进入聊天页 | 显示历史消息，消息气泡左右布局正确 |
| 3 | 输入文字并发送 | 消息发送成功，显示在右侧 |
| 4 | 发送图片 | 图片消息发送成功 |
| 5 | 收到新消息 | 消息自动追加到列表，滚动到底部 |

### TC-E2E-008: 个人中心

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 进入"我的"Tab | 显示个人信息卡片（头像、昵称、信用分、认证状态） |
| 2 | 点击功能入口"我的简历" | 跳转简历编辑页 |
| 3 | 点击功能入口"投递记录" | 跳转投递记录页 |
| 4 | 点击功能入口"安全中心" | 跳转安全中心页 |
| 5 | 点击"退出登录" | 清除token和用户信息，返回首页 |

---

## 五、JMeter性能测试脚本

### 5.1 测试场景配置

```yaml
test_plan:
  name: "长沙大学生兼职平台性能测试"
  duration: 300
  ramp_up_period: 60
  concurrency: 1000

thread_groups:
  - name: "岗位搜索场景"
    threads: 400
    duration: 300
    ramp_up: 60
  
  - name: "投递场景"
    threads: 300
    duration: 300
    ramp_up: 60
  
  - name: "登录场景"
    threads: 300
    duration: 300
    ramp_up: 60
```

### 5.2 岗位搜索接口测试脚本

```yaml
thread_group: "岗位搜索场景"

steps:
  - name: "获取测试用户Token"
    sampler:
      url: "https://api.parttime-cs.com/v1/auth/phone-login"
      method: POST
      body: '{"phone": "138${__Random(00000000,99999999)}", "code": "123456"}'
      headers:
        - "Content-Type: application/json"
    extractor:
      name: "token"
      regex: '"token":"(.*?)"'

  - name: "岗位搜索接口"
    sampler:
      url: "https://api.parttime-cs.com/v1/student/job/list"
      method: GET
      parameters:
        - "longitude=112.9388"
        - "latitude=28.2282"
        - "distance=5"
        - "page=1"
        - "size=20"
      headers:
        - "Authorization: Bearer ${token}"
    assertion:
      - response_code: 200
      - response_time: <= 200ms

assertions:
  - 90%响应时间: <= 200ms
  - 错误率: < 1%
```

### 5.3 投递接口测试脚本

```yaml
thread_group: "投递场景"

steps:
  - name: "获取测试用户Token"
    sampler:
      url: "https://api.parttime-cs.com/v1/auth/phone-login"
      method: POST
      body: '{"phone": "139${__Random(00000000,99999999)}", "code": "123456"}'
      headers:
        - "Content-Type: application/json"
    extractor:
      name: "token"
      regex: '"token":"(.*?)"'

  - name: "获取岗位列表"
    sampler:
      url: "https://api.parttime-cs.com/v1/public/job/list?page=1&size=10"
      method: GET
    extractor:
      name: "job_id"
      regex: '"job_id":"(.*?)"'
      match_number: random

  - name: "岗位投递接口"
    sampler:
      url: "https://api.parttime-cs.com/v1/student/apply"
      method: POST
      body: '{"job_id": "${job_id}"}'
      headers:
        - "Content-Type: application/json"
        - "Authorization: Bearer ${token}"
    assertion:
      - response_code: 200
      - response_time: <= 1000ms

assertions:
  - 90%响应时间: <= 1s
  - 错误率: < 1%
```

### 5.4 登录接口测试脚本

```yaml
thread_group: "登录场景"

steps:
  - name: "发送验证码"
    sampler:
      url: "https://api.parttime-cs.com/v1/auth/sms-code"
      method: POST
      body: '{"phone": "137${__Random(00000000,99999999)}"}'
      headers:
        - "Content-Type: application/json"
    assertion:
      - response_code: 200

  - name: "手机号登录接口"
    sampler:
      url: "https://api.parttime-cs.com/v1/auth/phone-login"
      method: POST
      body: '{"phone": "137${__Random(00000000,99999999)}", "code": "123456"}'
      headers:
        - "Content-Type: application/json"
    assertion:
      - response_code: 200
      - response_time: <= 500ms

assertions:
  - 90%响应时间: <= 500ms
  - 错误率: < 1%
```

### 5.5 JMeter结果验证标准

| 指标 | 岗位搜索 | 投递接口 | 登录接口 |
|------|---------|---------|---------|
| 90%响应时间 | ≤200ms | ≤1s | ≤500ms |
| 95%响应时间 | ≤300ms | ≤1.5s | ≤800ms |
| 99%响应时间 | ≤500ms | ≤2s | ≤1s |
| 错误率 | <1% | <1% | <1% |
| TPS | ≥500 | ≥200 | ≥300 |

### 5.6 测试环境配置

```yaml
system_properties:
  - "httpclient.socket.timeout=10000"
  - "httpclient.connect.timeout=5000"
  - "jmeter.threads.startup_delay=0"

distributed:
  master: "192.168.1.100"
  slaves:
    - "192.168.1.101"
    - "192.168.1.102"
    - "192.168.1.103"

monitoring:
  prometheus: "http://localhost:9090"
  grafana_dashboard: "兼职平台性能监控"
```

---

## 测试用例汇总

| 测试类型 | 用例数量 | 优先级 |
|----------|---------|--------|
| 功能测试 | 18个 | P0 |
| 安全测试 | 9个 | P0 |
| 合规测试 | 8个 | P1 |
| 小程序E2E测试 | 8个 | P1 |
| 性能测试 | 3个场景 | P1 |

> **总计**: 43个测试用例 + 3个性能测试场景