<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import {
  TrendCharts,
  Briefcase,
  CircleCheck,
  Wallet,
  Message,
  Setting,
  User,
  Search,
  ArrowLeft,
  CaretRight
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const store = useAdminStore()

const sidebarCollapsed = ref(false)
const searchKeyword = ref('')

const adminMenus = computed(() => {
  const menus: any[] = []
  const roleType = store.adminRoleType

  if (roleType === 1 || roleType === 5) {
    menus.push({
      path: '/admin/audit/enterprise',
      name: '审核管理',
      icon: Briefcase,
      children: [
        { path: '/admin/audit/enterprise', name: '企业审核' },
        { path: '/admin/audit/job', name: '岗位审核' },
      ]
    })
  }

  if (roleType === 2 || roleType === 5) {
    menus.push({
      path: '/admin/risk/complaint',
      name: '风控管理',
      icon: CircleCheck,
      children: [
        { path: '/admin/risk/complaint', name: '投诉工单' },
        { path: '/admin/risk/dashboard', name: '风控看板' },
      ]
    })
  }

  if (roleType === 3 || roleType === 5) {
    menus.push({
      path: '/admin/operation/report',
      name: '运营管理',
      icon: TrendCharts,
      children: [
        { path: '/admin/operation/report', name: '数据报表' },
        { path: '/admin/operation/notification', name: '推送管理' },
      ]
    })
  }

  if (roleType === 4 || roleType === 5) {
    menus.push({
      path: '/admin/finance/settlement',
      name: '财务管理',
      icon: Wallet,
      children: [
        { path: '/admin/finance/settlement', name: '薪资发放' },
        { path: '/admin/finance/report', name: '财务报表' },
      ]
    })
  }

  if (roleType === 5) {
    menus.push({
      path: '/admin/system/config',
      name: '系统管理',
      icon: Setting,
      children: [
        { path: '/admin/system/config', name: '系统配置' },
        { path: '/admin/system/roles', name: '角色权限' },
        { path: '/admin/system/audit-log', name: '审计日志' },
      ]
    })
  }

  return menus
})

const roleNames: Record<number, string> = {
  1: '审核管理员',
  2: '风控管理员',
  3: '运营管理员',
  4: '财务管理员',
  5: '超级管理员'
}

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

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    console.log('搜索:', searchKeyword.value)
  }
}
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="logo">
        <span class="logo-text" v-if="!sidebarCollapsed">兼职平台</span>
        <span class="logo-icon" v-else>管</span>
      </div>
      <nav class="menu">
        <div 
          v-for="item in adminMenus" 
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
          <div class="search-box" v-if="!sidebarCollapsed">
            <Search class="search-icon" />
            <input 
              v-model="searchKeyword"
              type="text" 
              placeholder="搜索..." 
              @keyup.enter="handleSearch"
            />
          </div>
        </div>
        <div class="right-section">
          <span class="role-label">{{ roleNames[store.adminRoleType] || '管理员' }}</span>
          <button class="icon-btn" @click="handleLogout">
            <Message />
          </button>
          <button class="icon-btn user-btn" @click="handleLogout">
            <User />
          </button>
          <button class="logout-btn" @click="handleLogout">
            <ArrowLeft />
            <span>退出</span>
          </button>
        </div>
      </header>

      <div class="content-wrapper">
        <router-view />
      </div>

      <footer class="footer">
        <span>© 2026 长沙大学生兼职平台 - 管理后台</span>
      </footer>
    </main>
  </div>
</template>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 240px;
  background-color: #0F172A;
  color: #F8FAFC;
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
  border-bottom: 1px solid #1E293B;
  font-size: 18px;
  font-weight: bold;
}

.logo-text {
  color: #165DFF;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #165DFF 0%, #0F172A 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #FFFFFF;
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
    background-color: rgba(255, 255, 255, 0.05);
  }

  &.active {
    background-color: rgba(22, 93, 255, 0.2);
    
    .menu-icon {
      color: #165DFF;
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
  color: #94A3B8;
}

.menu-text {
  font-size: 14px;
}

.menu-arrow {
  font-size: 12px;
  color: #94A3B8;
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
  color: #CBD5E1;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: rgba(255, 255, 255, 0.05);
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
  border-bottom: 1px solid #E2E8F0;
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
  color: #475569;
  cursor: pointer;
  margin-right: 16px;

  .el-icon {
    transition: transform 0.3s;

    &.rotated {
      transform: rotate(180deg);
    }
  }
}

.search-box {
  position: relative;
  width: 300px;

  .search-icon {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: #94A3B8;
  }

  input {
    width: 100%;
    height: 32px;
    padding: 0 12px 0 36px;
    border: 1px solid #E2E8F0;
    border-radius: 4px;
    font-size: 14px;
    background-color: #F8FAFC;
    outline: none;

    &:focus {
      border-color: #165DFF;
      background-color: #FFFFFF;
    }
  }
}

.right-section {
  display: flex;
  align-items: center;
}

.role-label {
  font-size: 13px;
  color: #64748B;
  padding: 4px 12px;
  background-color: #E0F2FE;
  color: #0369A1;
  border-radius: 4px;
  margin-right: 16px;
}

.icon-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #475569;
  cursor: pointer;
  margin-left: 16px;
  padding: 8px;

  &:hover {
    color: #165DFF;
  }
}

.user-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #F1F5F9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn {
  display: flex;
  align-items: center;
  background: none;
  border: 1px solid #E2E8F0;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 13px;
  color: #64748B;
  cursor: pointer;
  margin-left: 16px;
  transition: all 0.2s;

  &:hover {
    background-color: #F8FAFC;
    color: #EF4444;
    border-color: #FECACA;
  }

  .el-icon {
    margin-right: 4px;
  }
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
  background-color: #F1F5F9;
  padding: 24px;
}

.footer {
  height: 40px;
  background-color: #FFFFFF;
  border-top: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #94A3B8;
}
</style>