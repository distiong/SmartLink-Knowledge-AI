<template>
  <div class="user-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showAddDialog">新增用户</el-button>
    </div>
    
    <el-table :data="userList" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="userType" label="账号类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.userType === 1 ? 'danger' : 'success'">
            {{ row.userType === 1 ? '超级管理员' : '子账号' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="350">
        <template #default="{ row }">
          <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button size="small" type="primary" @click="goToPermission(row)" v-if="row.userType !== 1">权限配置</el-button>
          <el-button size="small" type="warning" @click="resetPassword(row)">重置密码</el-button>
          <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteUser(row)" v-if="row.userType !== 1">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userForm.roleId" placeholder="请选择角色">
            <el-option v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const userList = ref([])
const roleList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const userFormRef = ref(null)
const editingId = ref(null)

const getHeaders = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

const userForm = reactive({
  username: '',
  password: '',
  nickname: '',
  roleId: 3
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/user/list', getHeaders())
    if (response.data.code === 200) {
      userList.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  try {
    const response = await axios.get('/api/role/list', getHeaders())
    if (response.data.code === 200) {
      roleList.value = response.data.data
    }
  } catch (error) {
    console.error('获取角色列表失败')
  }
}

const showAddDialog = () => {
  isEdit.value = false
  editingId.value = null
  Object.assign(userForm, { username: '', password: '', nickname: '', roleId: 3 })
  dialogVisible.value = true
}

const showEditDialog = (user) => {
  isEdit.value = true
  editingId.value = user.id
  Object.assign(userForm, { username: user.username, nickname: user.nickname, roleId: user.roleId })
  dialogVisible.value = true
}

const submitUser = async () => {
  try {
    await userFormRef.value.validate()
    
    if (isEdit.value) {
      await axios.put(`/api/user/${editingId.value}`, userForm, getHeaders())
      ElMessage.success('更新成功')
    } else {
      await axios.post('/api/user', userForm, getHeaders())
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '操作失败')
  }
}

const resetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户 ${user.username} 的密码吗？`, '提示')
    
    await axios.put(`/api/user/${user.id}/reset-password`, { password: '123456' }, getHeaders())
    ElMessage.success('密码已重置为 123456')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置密码失败')
    }
  }
}

const toggleStatus = async (user) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    await axios.put(`/api/user/${user.id}/status/${newStatus}`, null, getHeaders())
    ElMessage.success('状态更新成功')
    fetchUsers()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？`, '提示')
    
    await axios.delete(`/api/user/${user.id}`, getHeaders())
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const goToPermission = (user) => {
  router.push(`/system/user/${user.id}/permission`)
}

onMounted(() => {
  fetchUsers()
  fetchRoles()
})
</script>

<style scoped>
.user-container {
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
</style>
