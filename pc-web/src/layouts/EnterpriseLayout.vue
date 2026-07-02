<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
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
  ArrowRight,
  SwitchButton,
  User,
  Search,
  ArrowDown
} from '@element-plus/icons-vue'
import { enterpriseSearchApi } from '@/api/enterprise'

const router = useRouter()
const route = useRoute()
const store = useEnterpriseStore()

const sidebarCollapsed = ref(false)
const userDropdownVisible = ref(false)
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const showSearchResults = ref(false)

const menuItems = [
  { path: '/enterprise/dashboard', name: '数据看板', icon: TrendCharts },
  { path: '/enterprise/job/list', name: '岗位列表', icon: Briefcase },
  { path: '/enterprise/job/publish', name: '发布岗位', icon: Briefcase },
  { path: '/enterprise/job/batch-import', name: '批量导入', icon: Briefcase },
  { path: '/enterprise/talent/apply', name: '投递管理', icon: UserFilled },
  { path: '/enterprise/talent/library', name: '人才库', icon: UserFilled },
  { path: '/enterprise/schedule', name: '排班管理', icon: Calendar },
  { path: '/enterprise/salary/calculate', name: '薪资核算', icon: Wallet },
  { path: '/enterprise/salary/records', name: '发放记录', icon: Wallet },
  { path: '/enterprise/finance/statement', name: '对账单', icon: Files },
  { path: '/enterprise/finance/invoice', name: '发票管理', icon: Files },
  { path: '/enterprise/agreement', name: '协议管理', icon: Files },
  { path: '/enterprise/settings', name: '设置', icon: Setting },
]

const activePath = computed(() => route.path)

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleLogout = () => {
  store.logout()
  router.push('/')
}

const toggleUserDropdown = () => {
  userDropdownVisible.value = !userDropdownVisible.value
}

const closeUserDropdown = () => {
  userDropdownVisible.value = false
}

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResults.value = []
    showSearchResults.value = false
    return
  }
  
  try {
    const response = await enterpriseSearchApi.searchMenus({ keyword })
    const data = response.data
    if (data.code === 200) {
      searchResults.value = data.data || []
    } else {
      searchResults.value = menuItems.filter(item => 
        item.name.toLowerCase().includes(keyword.toLowerCase())
      )
    }
  } catch {
    searchResults.value = menuItems.filter(item => 
      item.name.toLowerCase().includes(keyword.toLowerCase())
    )
  }
  showSearchResults.value = searchResults.value.length > 0
}

const handleSearchInput = () => {
  if (searchKeyword.value.trim()) {
    handleSearch()
  } else {
    searchResults.value = []
    showSearchResults.value = false
  }
}

const selectSearchResult = (item: any) => {
  router.push(item.path)
  searchKeyword.value = ''
  searchResults.value = []
  showSearchResults.value = false
}

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.search-box') && !target.closest('.user-info')) {
    closeUserDropdown()
    showSearchResults.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const breadcrumbs = computed(() => {
  const result: { name: string; path?: string }[] = [{ name: '首页' }]
  
  const match = menuItems.find(item => item.path === activePath.value)
  if (match) {
    result.push({ name: match.name, path: match.path })
  }
  
  return result
})
</script>

<template>
  <div class="enterprise-layout">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="logo">
        <div class="logo-icon">
          <span v-if="!sidebarCollapsed">长沙兼职</span>
          <span v-else>兼</span>
        </div>
      </div>
      <nav class="menu">
        <div 
          v-for="item in menuItems" 
          :key="item.path"
          class="menu-item"
          :class="{ active: activePath === item.path }"
          @click="router.push(item.path)"
        >
          <div class="menu-left">
            <component :is="item.icon" class="menu-icon" />
            <span class="menu-text" v-if="!sidebarCollapsed">{{ item.name }}</span>
          </div>
        </div>
      </nav>
      <div class="collapse-btn" @click="toggleSidebar">
        <ArrowRight :class="{ rotated: sidebarCollapsed }" />
      </div>
    </aside>

    <main class="main-content">
      <header class="top-bar">
        <div class="left-section">
          <button class="sidebar-toggle" @click="toggleSidebar">
            <CaretRight :class="{ rotated: !sidebarCollapsed }" />
          </button>
          <div class="breadcrumb">
            <span 
              v-for="(item, index) in breadcrumbs" 
              :key="index"
              class="breadcrumb-item"
              :class="{ clickable: item.path, last: index === breadcrumbs.length - 1 }"
              @click="item.path && router.push(item.path)"
            >
              {{ item.name }}
            </span>
          </div>
        </div>
        <div class="right-section">
          <div class="search-box" @click.stop>
            <button class="search-icon-btn" @click="handleSearch">
              <Search class="search-icon" />
            </button>
            <input 
              v-model="searchKeyword"
              type="text" 
              placeholder="搜索菜单、功能..." 
              @keyup.enter="handleSearch"
              @input="handleSearchInput"
            />
            <div class="search-results" v-if="showSearchResults" @click.stop>
              <div 
                v-for="item in searchResults" 
                :key="item.path"
                class="search-result-item"
                @click="selectSearchResult(item)"
              >
                <component :is="item.icon || Briefcase" class="result-icon" />
                <span class="result-name">{{ item.name }}</span>
              </div>
            </div>
          </div>
          
          <div class="user-info" @click.stop="toggleUserDropdown">
            <div class="avatar">
              <User />
            </div>
            <div class="user-detail" v-if="!sidebarCollapsed">
              <span class="user-name">{{ store.enterpriseName || '企业用户' }}</span>
              <span class="user-role">企业账号</span>
            </div>
            <ArrowDown class="user-arrow" :class="{ open: userDropdownVisible }" />
          </div>
          <div class="user-dropdown" v-if="userDropdownVisible" @click.stop>
            <div class="dropdown-header">
              <div class="dropdown-avatar">
                <User />
              </div>
              <div class="dropdown-user-info">
                <span class="dropdown-username">{{ store.enterpriseName || '企业用户' }}</span>
                <span class="dropdown-role">企业运营端</span>
              </div>
            </div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item" @click="router.push('/enterprise/profile'); closeUserDropdown()">
              <User class="dropdown-icon" />
              <span>个人中心</span>
            </div>
            <div class="dropdown-item" @click="router.push('/enterprise/settings'); closeUserDropdown()">
              <Setting class="dropdown-icon" />
              <span>账号设置</span>
            </div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item logout" @click="handleLogout">
              <SwitchButton class="dropdown-icon" />
              <span>退出登录</span>
            </div>
          </div>
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
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
}

.sidebar {
  width: 220px;
  background-color: #001529;
  color: #FFFFFF;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 100;

  &.collapsed {
    width: 64px;
  }
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 18px;
  background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  
  span {
    &:first-child {
      -webkit-text-fill-color: #FFFFFF;
      background: none;
    }
  }
}

.sidebar.collapsed .logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
  border-radius: 8px;
  -webkit-background-clip: padding-box;
  -webkit-text-fill-color: #FFFFFF;
  
  span {
    -webkit-text-fill-color: #FFFFFF;
  }
}

.menu {
  flex: 1;
  padding: 12px 0;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.15);
    border-radius: 2px;
  }
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 44px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  margin: 2px 8px;
  border-radius: 6px;

  &:hover {
    background-color: #1F2D3D;
  }

  &.active {
    background-color: #165DFF;
    
    .menu-icon,
    .menu-text {
      color: #FFFFFF;
    }

    .menu-arrow {
      color: rgba(255, 255, 255, 0.85);
    }
  }
}

.menu-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.menu-icon {
  width: 18px;
  height: 18px;
  margin-right: 12px;
  color: rgba(255, 255, 255, 0.65);
  flex-shrink: 0;
}

.sidebar.collapsed .menu-icon {
  margin-right: 0;
}

.menu-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.menu-arrow {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
  transition: transform 0.3s ease;
  flex-shrink: 0;

  &.expanded {
    transform: rotate(90deg);
  }
}

.sub-menu {
  width: 100%;
  position: absolute;
  left: 0;
  top: 100%;
  background-color: #000C17;
  border-radius: 0 0 6px 6px;
  padding: 4px 0;
  z-index: 10;
}

.sidebar.collapsed .sub-menu {
  display: none;
}

.sub-menu-item {
  padding: 0 16px 0 46px;
  height: 36px;
  line-height: 36px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: rgba(255, 255, 255, 0.06);
    color: rgba(255, 255, 255, 0.85);
  }

  &.active {
    color: #165DFF;
    background-color: rgba(22, 93, 255, 0.1);
  }
}

.collapse-btn {
  position: absolute;
  right: -10px;
  top: 80px;
  transform: translateY(0);
  width: 20px;
  height: 20px;
  background-color: #FFFFFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  z-index: 10;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .el-icon {
    font-size: 12px;
    color: #4E5969;
    transition: transform 0.3s ease;

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
  background-color: #F2F3F5;
}

.top-bar {
  height: 56px;
  background-color: #FFFFFF;
  border-bottom: 1px solid #E5E6EB;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.left-section {
  display: flex;
  align-items: center;
}

.sidebar-toggle {
  background: none;
  border: none;
  font-size: 16px;
  color: #4E5969;
  cursor: pointer;
  margin-right: 16px;
  padding: 6px;
  border-radius: 6px;
  transition: all 0.2s ease;

  &:hover {
    background-color: #F2F3F5;
    color: #165DFF;
  }

  .el-icon {
    transition: transform 0.3s ease;
    display: block;

    &.rotated {
      transform: rotate(180deg);
    }
  }
}

.breadcrumb {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.breadcrumb-item {
  color: #86909C;
  position: relative;
  padding-right: 24px;

  &:not(:last-child)::after {
    content: '/';
    position: absolute;
    right: 8px;
    color: #C9CDD4;
  }

  &.clickable {
    cursor: pointer;
    transition: color 0.2s ease;

    &:hover {
      color: #165DFF;
    }
  }

  &.last {
    color: #1D2129;
    font-weight: 500;
  }
}

.right-section {
  display: flex;
  align-items: center;
  position: relative;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  width: 280px;
  height: 36px;
  border: 1px solid #E5E6EB;
  border-radius: 6px;
  background-color: #F7F8FA;
  overflow: hidden;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  margin-right: 12px;

  &:focus-within {
    border-color: #165DFF;
    background-color: #FFFFFF;
    box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
  }

  .search-icon-btn {
    width: 36px;
    height: 100%;
    background: transparent;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #86909C;
    transition: all 0.15s;

    &:hover {
      color: #4E5969;
    }
  }

  .search-icon {
    width: 16px;
    height: 16px;
  }

  input {
    flex: 1;
    height: 100%;
    padding: 0 12px;
    border: none;
    font-size: 13px;
    background: transparent;
    outline: none;

    &::placeholder {
      color: #C9CDD4;
    }
  }
}

.search-results {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background-color: #FFFFFF;
  border: 1px solid #E5E6EB;
  border-radius: 6px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  z-index: 1000;
  overflow: hidden;
  animation: dropdownFadeIn 0.15s ease;

  &::before {
    content: '';
    position: absolute;
    top: -6px;
    left: 16px;
    width: 12px;
    height: 12px;
    background-color: #FFFFFF;
    border-top: 1px solid #E5E6EB;
    border-left: 1px solid #E5E6EB;
    transform: rotate(45deg);
  }
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  cursor: pointer;
  transition: all 0.12s;
  gap: 10px;

  &:hover {
    background-color: #F7F8FA;

    .result-name {
      color: #165DFF;
    }
  }

  &:active {
    background-color: #EBF5FF;
  }
}

.result-icon {
  width: 16px;
  height: 16px;
  color: #86909C;
  flex-shrink: 0;
}

.result-name {
  font-size: 13px;
  color: #4E5969;
  transition: color 0.12s;
}

.notification-btn {
  position: relative;
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #86909C;
  transition: all 0.15s;

  &:hover {
    background-color: #F7F8FA;
    color: #4E5969;
  }

  &:active {
    transform: scale(0.95);
  }
}

.notification-icon {
  width: 18px;
  height: 18px;
}

.notification-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background-color: #F53F3F;
  color: #FFFFFF;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 52px;
  width: 320px;
  background-color: #FFFFFF;
  border: 1px solid #E5E6EB;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  overflow: hidden;
  animation: dropdownFadeIn 0.2s ease;

  &::before {
    content: '';
    position: absolute;
    top: -6px;
    right: 20px;
    width: 12px;
    height: 12px;
    background-color: #FFFFFF;
    border-top: 1px solid #E5E6EB;
    border-right: 1px solid #E5E6EB;
    transform: rotate(-45deg);
  }
}

.dropdown-title {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #24292F;
  border-bottom: 1px solid #E5E6EB;
}

.notification-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.12s;
  gap: 12px;

  &:hover {
    background-color: #F7F8FA;
  }

  &:active {
    background-color: #EBF5FF;
  }
}

.notification-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .el-icon {
    width: 16px;
    height: 16px;
  }

  &.success {
    background-color: #E8FFEA;
    .el-icon { color: #00B42A; }
  }

  &.warning {
    background-color: #FFF7E6;
    .el-icon { color: #FF7D00; }
  }

  &.danger {
    background-color: #FFF1F0;
    .el-icon { color: #F53F3F; }
  }
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 13px;
  color: #24292F;
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-time {
  font-size: 11px;
  color: #C9CDD4;
}

.dropdown-footer {
  padding: 12px 16px;
  border-top: 1px solid #E5E6EB;
  text-align: center;
}

.view-all {
  font-size: 12px;
  color: #165DFF;
  text-decoration: none;
  transition: color 0.15s;

  &:hover {
    color: #0E42D2;
  }
}

.user-info {
  display: flex;
  align-items: center;
  padding: 6px 12px 6px 6px;
  margin-left: 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    background-color: #F2F3F5;
  }

  &:active {
    background-color: #E5E6EB;
  }
}

.avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 16px;
  flex-shrink: 0;
}

.user-detail {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
  margin-right: 8px;
}

.user-name {
  font-size: 14px;
  color: #1D2129;
  font-weight: 500;
  line-height: 1.2;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: #86909C;
  line-height: 1.2;
  margin-top: 2px;
}

.user-arrow {
  width: 14px;
  height: 14px;
  color: #86909C;
  transition: transform 0.2s cubic-bezier(0.16, 1, 0.3, 1);

  &.open {
    transform: rotate(180deg);
  }
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  background-color: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  overflow: hidden;
  animation: dropdownFadeIn 0.2s cubic-bezier(0.16, 1, 0.3, 1);

  &::before {
    content: '';
    position: absolute;
    top: -6px;
    right: 28px;
    width: 12px;
    height: 12px;
    background-color: #FFFFFF;
    border-top: 1px solid #E5E6EB;
    border-right: 1px solid #E5E6EB;
    transform: rotate(-45deg);
  }
}

@keyframes dropdownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-8px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dropdown-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #F7F8FA;
}

.dropdown-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #165DFF 0%, #3C7EFF 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 18px;
  flex-shrink: 0;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
  margin-left: 12px;
  min-width: 0;
}

.dropdown-username {
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-role {
  font-size: 12px;
  color: #86909C;
  margin-top: 2px;
}

.dropdown-divider {
  height: 1px;
  background-color: #E5E6EB;
  margin: 4px 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.12s;
  font-size: 14px;
  color: #4E5969;
  gap: 10px;

  &:hover {
    background-color: #F2F3F5;
    color: #165DFF;

    .dropdown-icon {
      color: #165DFF;
    }
  }

  &:active {
    background-color: #EBF5FF;
  }

  &.logout:hover {
    background-color: #FFF1F0;
    color: #F53F3F;

    .dropdown-icon {
      color: #F53F3F;
    }
  }
}

.dropdown-icon {
  width: 16px;
  height: 16px;
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.footer {
  height: 40px;
  background-color: #FFFFFF;
  border-top: 1px solid #E5E6EB;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #86909C;
  flex-shrink: 0;
}

@media (max-width: 1024px) {
  .sidebar {
    width: 64px;

    .menu-text,
    .menu-arrow,
    .sub-menu,
    .logo-icon span:first-child {
      display: none;
    }

    .menu-icon {
      margin-right: 0;
    }
  }

  .sidebar.collapsed {
    width: 0;
    overflow: hidden;
  }

  .user-detail {
    display: none;
  }
}
</style>
