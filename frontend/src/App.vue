<template>
  <div id="app">
    <el-container v-if="isLoggedIn">
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <h2>AI知识库系统</h2>
        </div>
        <el-menu
          :default-active="activeIndex"
          router
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>数据看板</span>
          </el-menu-item>

          <el-menu-item index="/chat">
            <el-icon><ChatLineRound /></el-icon>
            <span>AI对话</span>
          </el-menu-item>
          
          <el-sub-menu index="rag">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>知识库</span>
            </template>
            <el-menu-item index="/rag/chat">RAG问答</el-menu-item>
            <el-menu-item index="/rag/document">文档管理</el-menu-item>
            <el-menu-item index="/rag/category">分类管理</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/search">
            <el-icon><Search /></el-icon>
            <span>向量检索</span>
          </el-menu-item>
          
          <el-menu-item index="/graph">
            <el-icon><Share /></el-icon>
            <span>知识图谱</span>
          </el-menu-item>
          
          <el-sub-menu index="system" v-if="isAdmin">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/user">用户管理</el-menu-item>
            <el-menu-item index="/system/role">角色管理</el-menu-item>
            <el-menu-item index="/system/log">操作日志</el-menu-item>
            <el-menu-item index="/system/model">模型管理</el-menu-item>
            <el-menu-item index="/system/config">系统配置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-content">
            <div class="header-left">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="header-right">
              <el-switch
                v-model="isDark"
                inline-prompt
                :active-icon="Moon"
                :inactive-icon="Sunny"
                @change="toggleDark"
                style="margin-right: 16px"
              />
              <el-dropdown @command="handleCommand">
                <span class="user-info">
                  <el-icon><User /></el-icon>
                  {{ userInfo.nickname || userInfo.username || '用户' }}
                  <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        <el-main>
          <router-view :key="routerKey" />
        </el-main>
      </el-container>
    </el-container>
    <router-view v-else />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDark, useToggle } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import { Moon, Sunny } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userInfo = ref({})
const isLoggedIn = ref(false)
const routerKey = ref(0)

const isDark = useDark()
const toggleDark = useToggle(isDark)

const activeIndex = computed(() => route.path)
const isAdmin = computed(() => userInfo.value.userType === 1)

const currentTitle = computed(() => {
  const titleMap = {
    '/dashboard': '数据看板',
    '/chat': 'AI对话',
    '/rag/chat': 'RAG问答',
    '/rag/document': '文档管理',
    '/rag/category': '分类管理',
    '/search': '向量检索',
    '/graph': '知识图谱',
    '/system/user': '用户管理',
    '/system/role': '角色管理',
    '/system/log': '操作日志',
    '/system/model': '模型管理',
    '/system/config': '系统配置'
  }
  return titleMap[route.path] || ''
})

const checkLogin = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
  if (token) {
    fetchUserInfo()
  } else {
    userInfo.value = {}
  }
}

const fetchUserInfo = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      isLoggedIn.value = false
      return
    }
    
    const response = await axios.get('/api/auth/info', {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.code === 200) {
      userInfo.value = response.data.data
    } else {
      // token无效，清除登录状态
      logout()
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    if (error.response?.status === 401 || error.response?.status === 403) {
      logout()
    }
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  userInfo.value = {}
  isLoggedIn.value = false
  routerKey.value++
  router.push('/login')
}

const handleCommand = (command) => {
  if (command === 'logout') {
    logout()
    ElMessage.success('已退出登录')
  }
}

// 监听路由变化，检查登录状态
watch(() => route.path, () => {
  checkLogin()
})

onMounted(() => {
  checkLogin()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

#app {
  min-height: 100vh;
}

.sidebar {
  background-color: #304156;
  min-height: 100vh;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #263445;
}

.logo h2 {
  color: #fff;
  font-size: 16px;
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none !important;
}

.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
}

.sidebar-menu .el-menu-item:hover {
  background-color: #263445 !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #1890ff !important;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  line-height: 60px;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
  font-size: 14px;
}

.user-info .el-icon {
  margin-right: 5px;
}

.el-main {
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
  padding: 20px;
}

/* 暗色模式样式 */
html.dark {
  color-scheme: dark;
}

html.dark body {
  background-color: #1a1a1a;
  color: #e5eaf3;
}

html.dark .el-header {
  background-color: #1d1e1f;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

html.dark .el-main {
  background-color: #0a0a0a;
}

html.dark .el-card {
  background-color: #1d1e1f;
  border-color: #333;
}

html.dark .el-card__header {
  border-bottom-color: #333;
}

html.dark .el-input__wrapper {
  background-color: #303030;
  box-shadow: 0 0 0 1px #4c4d4f inset;
}

html.dark .el-input__inner {
  color: #e5eaf3;
}

html.dark .el-textarea__inner {
  background-color: #303030;
  color: #e5eaf3;
  border-color: #4c4d4f;
}

html.dark .el-table {
  background-color: #1d1e1f;
  color: #e5eaf3;
}

html.dark .el-table th.el-table__cell {
  background-color: #262727;
  color: #e5eaf3;
  border-bottom-color: #333;
}

html.dark .el-table td.el-table__cell {
  border-bottom-color: #333;
}

html.dark .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background-color: #262727;
}

html.dark .el-table__body tr:hover > td.el-table__cell {
  background-color: #333;
}

html.dark .el-dialog {
  background-color: #1d1e1f;
}

html.dark .el-dialog__header {
  border-bottom-color: #333;
}

html.dark .el-dialog__title {
  color: #e5eaf3;
}

html.dark .el-form-item__label {
  color: #e5eaf3;
}

html.dark .el-divider {
  border-color: #333;
}

html.dark .el-divider__text {
  background-color: #1d1e1f;
  color: #909399;
}

html.dark .el-tag {
  border-color: #4c4d4f;
}

html.dark .el-tag--info {
  background-color: #333;
  color: #e5eaf3;
}

html.dark .el-dropdown-menu {
  background-color: #1d1e1f;
  border-color: #333;
}

html.dark .el-dropdown-menu__item {
  color: #e5eaf3;
}

html.dark .el-dropdown-menu__item:hover {
  background-color: #333;
}

html.dark .el-breadcrumb__inner {
  color: #909399;
}

html.dark .el-breadcrumb__inner.is-link:hover {
  color: #409eff;
}

html.dark .el-pagination {
  color: #909399;
}

html.dark .el-pagination button:disabled {
  background-color: #333;
  color: #666;
}

html.dark .el-pager li {
  background-color: #333;
  color: #909399;
}

html.dark .el-pager li.is-active {
  background-color: #409eff;
  color: #fff;
}

html.dark .el-menu {
  background-color: #1d1e1f;
  border-right-color: #333;
}

html.dark .el-menu-item {
  color: #909399;
}

html.dark .el-menu-item:hover {
  background-color: #333;
}

html.dark .el-menu-item.is-active {
  color: #409eff;
}

html.dark .el-sub-menu__title {
  color: #909399;
}

html.dark .el-sub-menu__title:hover {
  background-color: #333;
}

html.dark .user-info {
  color: #e5eaf3;
}

html.dark .message-bubble {
  background-color: #333;
  color: #e5eaf3;
}

html.dark .user-message .message-bubble {
  background-color: #409eff;
  color: #fff;
}

html.dark .context-content {
  background-color: #262727;
  color: #909399;
}

html.dark .session-item:hover {
  background-color: #333;
}

html.dark .session-item.active {
  background-color: #1a3a5c;
  border-color: #1a5c9e;
}

html.dark .relation-item {
  background-color: #262727;
}

html.dark .el-empty__description p {
  color: #909399;
}
</style>
