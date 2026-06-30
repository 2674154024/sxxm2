# 文档偏差记录

## 1. Vue版本变更

**原文要求**: 小程序端使用Vue2版本 (`vue: 2.6.14`)

**实际实现**: 使用Vue3版本 (`vue: ^3.4.21`)

**原因**: 
- `@dcloudio/uni-app` 3.x 版本仅支持 Vue3，不支持 Vue2
- 原文档指定的 `@dcloudio/uni-app@2.0.2-4020420240722002` 在npm上不存在
- 官方推荐使用Vue3开发新项目

**影响**:
- 所有组件使用 `<script setup lang="ts">` 语法
- 状态管理从 Vuex 3.x 改为 Pinia 2.x

---

## 2. 状态管理变更

**原文要求**: 使用 Vuex 3.6.2

**实际实现**: 使用 Pinia 2.1.7

**原因**:
- Pinia 是 Vue3 官方推荐的状态管理方案
- 与 Vue3 Composition API 无缝集成
- 类型支持更好

**影响**:
- store 定义方式变更：`defineStore` 替代 `new Vuex.Store`
- 组件中使用 `useUserStore()` 替代 `mapState/mapActions`
- `store.dispatch('action')` 改为 `store.action()`

---

## 3. 依赖版本调整

### 3.1 小程序端依赖调整

| 依赖 | 文档版本 | 实际版本 | 说明 |
|------|---------|---------|------|
| `vue` | 2.6.14 | ^3.4.21 | Vue3 兼容 uni-app 3.x |
| `vuex` | 3.6.2 | - | 移除，改用 Pinia |
| `pinia` | - | ^2.1.7 | 新增，Vue3 状态管理 |
| `axios` | 0.27.2 | ^1.6.3 | 更新版本以兼容 Vue3 |
| `@dcloudio/uni-app` | 2.0.2-4020420240722002 | 3.0.0-4010520240507001 | 更新到可用版本 |

---

## 4. 项目结构调整

**原文要求**: pages.json、manifest.json、App.vue、main.ts 在项目根目录

**实际实现**: 统一放在 `src/` 目录下

**原因**:
- uni-app 3.x Vite 模式要求入口文件在 src 目录
- 符合现代前端项目规范

---

## 5. TabBar图标

**原文要求**: 使用自定义 PNG 图标

**实际实现**: 使用占位 PNG 图标

**原因**:
- 初始化阶段无法生成真实图标
- 后续可替换为真实图标资源

---

## 6. 注意事项

### 6.1 开发规范
- 所有组件必须使用 Vue3 Composition API (`<script setup>`)
- 状态管理使用 Pinia 的 `useUserStore()` 函数
- 路由跳转使用 `uni.navigateTo()` 等 uni-app API
- 样式使用 scoped 属性避免样式污染

### 6.2 与后端对接
- API 基础路径配置在环境变量中
- 请求拦截器已配置 JWT token 自动携带
- 响应统一格式处理已实现（code/message/data/trace_id）

### 6.3 构建命令
```bash
# H5 开发
npm run dev:h5

# H5 构建
npm run build:h5

# 微信小程序构建
npm run build:mp-weixin

# 支付宝小程序构建
npm run build:mp-alipay
```