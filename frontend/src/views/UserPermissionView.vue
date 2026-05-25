<template>
  <div class="permission-container">
    <div class="page-header">
      <h2>用户权限配置</h2>
      <el-button @click="goBack">返回用户列表</el-button>
    </div>
    
    <div class="user-info" v-if="userInfo">
      <el-descriptions title="用户信息" :column="3" border>
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userInfo.nickname }}</el-descriptions-item>
        <el-descriptions-item label="账号类型">
          <el-tag :type="userInfo.userType === 1 ? 'danger' : 'success'">
            {{ userInfo.userType === 1 ? '超级管理员' : '子账号' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </div>
    
    <div class="permission-section">
      <el-card class="permission-card">
        <template #header>
          <div class="card-header">
            <span>菜单权限</span>
            <el-button type="primary" size="small" @click="selectAllMenu">全选</el-button>
          </div>
        </template>
        <el-checkbox-group v-model="checkedMenuPerms">
          <div v-for="item in allPermissions.menuPermissions" :key="item.code" class="perm-item">
            <el-checkbox :label="item.code">{{ item.name }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </el-card>
      
      <el-card class="permission-card">
        <template #header>
          <div class="card-header">
            <span>按钮权限</span>
            <el-button type="primary" size="small" @click="selectAllButton">全选</el-button>
          </div>
        </template>
        <el-checkbox-group v-model="checkedButtonPerms">
          <div v-for="item in allPermissions.buttonPermissions" :key="item.code" class="perm-item">
            <el-checkbox :label="item.code">{{ item.name }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </el-card>
      
      <el-card class="permission-card">
        <template #header>
          <div class="card-header">
            <span>功能权限</span>
            <el-button type="primary" size="small" @click="selectAllFunction">全选</el-button>
          </div>
        </template>
        <el-checkbox-group v-model="checkedFunctionPerms">
          <div v-for="item in allPermissions.functionPermissions" :key="item.code" class="perm-item">
            <el-checkbox :label="item.code">{{ item.name }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </el-card>
    </div>
    
    <div class="action-bar">
      <el-button type="primary" size="large" @click="savePermissions">保存权限配置</el-button>
      <el-button size="large" @click="resetPermissions">重置</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const userId = route.params.id
const userInfo = ref(null)
const allPermissions = reactive({
  menuPermissions: [],
  buttonPermissions: [],
  functionPermissions: []
})

const checkedMenuPerms = ref([])
const checkedButtonPerms = ref([])
const checkedFunctionPerms = ref([])

const getHeaders = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

const fetchUserInfo = async () => {
  try {
    const response = await axios.get(`/api/user/${userId}`, getHeaders())
    if (response.data.code === 200) {
      userInfo.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

const fetchAllPermissions = async () => {
  try {
    const response = await axios.get('/api/user/permissions/all', getHeaders())
    if (response.data.code === 200) {
      Object.assign(allPermissions, response.data.data)
    }
  } catch (error) {
    ElMessage.error('获取权限列表失败')
  }
}

const fetchUserPermissions = async () => {
  try {
    const response = await axios.get(`/api/user/${userId}/permissions`, getHeaders())
    if (response.data.code === 200) {
      const perms = response.data.data
      checkedMenuPerms.value = perms.filter(p => p.permType === 'menu').map(p => p.permCode)
      checkedButtonPerms.value = perms.filter(p => p.permType === 'button').map(p => p.permCode)
      checkedFunctionPerms.value = perms.filter(p => p.permType === 'function').map(p => p.permCode)
    }
  } catch (error) {
    ElMessage.error('获取用户权限失败')
  }
}

const selectAllMenu = () => {
  if (checkedMenuPerms.value.length === allPermissions.menuPermissions.length) {
    checkedMenuPerms.value = []
  } else {
    checkedMenuPerms.value = allPermissions.menuPermissions.map(p => p.code)
  }
}

const selectAllButton = () => {
  if (checkedButtonPerms.value.length === allPermissions.buttonPermissions.length) {
    checkedButtonPerms.value = []
  } else {
    checkedButtonPerms.value = allPermissions.buttonPermissions.map(p => p.code)
  }
}

const selectAllFunction = () => {
  if (checkedFunctionPerms.value.length === allPermissions.functionPermissions.length) {
    checkedFunctionPerms.value = []
  } else {
    checkedFunctionPerms.value = allPermissions.functionPermissions.map(p => p.code)
  }
}

const savePermissions = async () => {
  try {
    const permissions = []
    
    checkedMenuPerms.value.forEach(code => {
      const item = allPermissions.menuPermissions.find(p => p.code === code)
      if (item) {
        permissions.push({ permCode: code, permName: item.name, permType: 'menu' })
      }
    })
    
    checkedButtonPerms.value.forEach(code => {
      const item = allPermissions.buttonPermissions.find(p => p.code === code)
      if (item) {
        permissions.push({ permCode: code, permName: item.name, permType: 'button' })
      }
    })
    
    checkedFunctionPerms.value.forEach(code => {
      const item = allPermissions.functionPermissions.find(p => p.code === code)
      if (item) {
        permissions.push({ permCode: code, permName: item.name, permType: 'function' })
      }
    })
    
    await axios.post(`/api/user/${userId}/permissions`, { permissions }, getHeaders())
    ElMessage.success('权限配置成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '权限配置失败')
  }
}

const resetPermissions = () => {
  fetchUserPermissions()
}

const goBack = () => {
  router.push('/system/user')
}

onMounted(() => {
  fetchUserInfo()
  fetchAllPermissions()
  fetchUserPermissions()
})
</script>

<style scoped>
.permission-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.user-info {
  margin-bottom: 20px;
}

.permission-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.permission-card {
  height: fit-content;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.perm-item {
  margin: 10px 0;
}

.action-bar {
  text-align: center;
  padding: 20px;
}
</style>
