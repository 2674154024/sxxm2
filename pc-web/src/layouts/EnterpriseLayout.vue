<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useEnterpriseStore } from '@/stores/enterprise'
import {
  TrendCharts,
  Briefcase,
  UserFilled,
  Calendar,
  Wallet,
  Files,
  Setting,
  Message,
  CaretRight
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const store = useEnterpriseStore()

const sidebarCollapsed = ref(false)

const menuItems = [
  { path: '/enterprise/dashboard', name: '数据看板', icon: TrendCharts },
  { 
    path: '/enterprise/job/list', 
    name: '岗位管理', 
    icon: Briefcase,
    children: [
      { path: '/enterprise/job/list', name: '岗位列表' },
      { path: '/enterprise/job/publish', name: '发布岗位' },
      { path: '/enterprise/job/batch-import', name: '批量导入' },
    ]
  },
  { 
    path: '/enterprise/talent/apply', 
    name: '人才管理', 
    icon: UserFilled,
    children: [
      { path: '/enterprise/talent/apply', name: '投递管理' },
      { path: '/enterprise/talent/library', name: '人才库' },
    ]
  },
  { path: '/enterprise/schedule', name: '排班管理', icon: Calendar },
  { 
    path: '/enterprise/salary/calculate', 
    name: '薪资管理', 
    icon: Wallet,
    children: [
      { path: '/enterprise/salary/calculate', name: '薪资核算' },
      { path: '/enterprise/salary/records', name: '发放记录' },
    ]
  },
  { path: '/enterprise/finance/statement', 
    name: '财务管理', 
    icon: Files,
    children: [
      { path: '/enterprise/finance/statement', name: '对账单' },
      { path: '/enterprise/finance/invoice', name: '发票管理' },
    ]
  },
  { path: '/enterprise/agreement', name: '协议管理', icon: Files },
  { path: '/enterprise/settings', name: '设置', icon: Setting },
]

const activePath = computed(() => route.path)

const hasChildren = (item: any) => item.children && item.children.length > 0

const handleMenuClick = (item: any) => {
  if (hasChildren(item)) {
    const firstChild = item.children[0]
    router.push(firstChild.path)
  } else {
    router.push(item.path)
  }
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleLogout = () => {
  store.logout()
  router.push('/')
}
</script>

<template>
  <div class="enterprise-layout">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="logo">
        <span class="logo-text" v-if="!sidebarCollapsed">长沙兼职</span>
        <span class="logo-icon" v-else>兼</span>
      </div>
      <nav class="menu">
        <div 
          v-for="item in menuItems" 
          :key="item.path"
          class="menu-item"
          :class="{ 
            active: activePath.includes(item.path),
            'has-children': hasChildren(item)
          }"
          @click="handleMenuClick(item)"
        >
          <component :is="item.icon" class="menu-icon" />
          <span class="menu-text" v-if="!sidebarCollapsed">{{ item.name }}</span>
          <CaretRight 
            class="menu-arrow" 
            :class="{ expanded: activePath.includes(item.path) }"
            v-if="hasChildren(item) && !sidebarCollapsed"
          />
          <div class="sub-menu" v-if="hasChildren(item) && activePath.includes(item.path) && !sidebarCollapsed">
            <div 
              v-for="child in item.children" 
              :key="child.path"
              class="sub-menu-item"
              :class="{ active: activePath === child.path }"
              @click.stop="router.push(child.path)"
            >
              {{ child.name }}
            </div>
          </div>
        </div>
      </nav>
      <div class="collapse-btn" @click="toggleSidebar">
        <CaretRight :class="{ rotated: sidebarCollapsed }" />
      </div>
    </aside>

    <main class="main-content">
      <header class="top-bar">
        <div class="left-section">
          <button class="sidebar-toggle" @click="toggleSidebar">
            <CaretRight :class="{ rotated: !sidebarCollapsed }" />
          </button>
          <span class="breadcrumb">{{ store.enterpriseName || '企业端' }}</span>
        </div>
        <div class="right-section">
          <button class="icon-btn" @click="handleLogout">
            <Message />
          </button>
          <button class="icon-btn" @click="handleLogout">
            <Briefcase />
          </button>
        </div>
      </header>

      <div class="content-wrapper">
        <router-view />
      </div>

      <footer class="footer">
        <span>© 2026 长沙大学生兼职平台 - 企业运营端</span>
      </footer>
    </main>
  </div>
</template>

<style scoped lang="scss">
.enterprise-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 240px;
  background-color: #1F2329;
  color: #FFFFFF;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  position: relative;

  &.collapsed {
    width: 64px;
  }
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #30363D;
  font-size: 20px;
  font-weight: bold;
}

.logo-text {
  color: #FFFFFF;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background-color: #165DFF;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.menu {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;

  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }

  &.active {
    background-color: #165DFF;
    
    .menu-icon {
      color: #FFFFFF;
    }
  }

  &.has-children {
    justify-content: space-between;
  }
}

.menu-icon {
  width: 20px;
  height: 20px;
  margin-right: 12px;
  color: #86909C;
}

.menu-text {
  font-size: 14px;
}

.menu-arrow {
  font-size: 12px;
  color: #86909C;
  transition: transform 0.3s;

  &.expanded {
    transform: rotate(90deg);
  }
}

.sub-menu {
  background-color: rgba(0, 0, 0, 0.2);
  margin: 8px 16px;
  border-radius: 4px;
  overflow: hidden;
}

.sub-menu-item {
  padding: 10px 20px;
  font-size: 13px;
  color: #C9CDD4;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }

  &.active {
    background-color: rgba(22, 93, 255, 0.3);
    color: #FFFFFF;
  }
}

.collapse-btn {
  position: absolute;
  right: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  background-color: #FFFFFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

  .el-icon {
    font-size: 14px;
    color: #4E5969;
    transition: transform 0.3s;

    &.rotated {
      transform: rotate(180deg);
    }
  }
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-bar {
  height: 64px;
  background-color: #FFFFFF;
  border-bottom: 1px solid #F2F3F5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.left-section {
  display: flex;
  align-items: center;
}

.sidebar-toggle {
  background: none;
  border: none;
  font-size: 18px;
  color: #4E5969;
  cursor: pointer;
  margin-right: 16px;

  .el-icon {
    transition: transform 0.3s;

    &.rotated {
      transform: rotate(180deg);
    }
  }
}

.breadcrumb {
  font-size: 14px;
  color: #4E5969;
}

.right-section {
  display: flex;
  align-items: center;
}

.icon-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #4E5969;
  cursor: pointer;
  margin-left: 24px;
  padding: 8px;

  &:hover {
    color: #165DFF;
  }
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
  background-color: #F2F3F5;
  padding: 24px;
}

.footer {
  height: 40px;
  background-color: #FFFFFF;
  border-top: 1px solid #F2F3F5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #86909C;
}
</style>