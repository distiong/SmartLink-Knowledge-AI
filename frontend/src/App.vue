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
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userInfo = ref({})
const isLoggedIn = ref(false)
const routerKey = ref(0)

const activeIndex = computed(() => route.path)
const isAdmin = computed(() => userInfo.value.userType === 1)

const currentTitle = computed(() => {
  const titleMap = {
    '/chat': 'AI对话',
    '/rag/chat': 'RAG问答',
    '/rag/document': '文档管理',
    '/search': '向量检索',
    '/graph': '知识图谱',
    '/system/user': '用户管理',
    '/system/role': '角色管理'
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
</style>
