<template>
  <div class="role-container">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="showAddDialog">新增角色</el-button>
    </div>
    
    <el-table :data="roleList" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleName" label="角色名称" width="150" />
      <el-table-column prop="roleCode" label="角色标识" width="120" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button size="small" type="primary" @click="showPermissionDialog(row)">权限配置</el-button>
          <el-button size="small" type="danger" @click="deleteRole(row)" v-if="row.id > 3">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleCode">
          <el-input v-model="roleForm.roleCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRole">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="permDialogVisible" title="权限配置" width="600px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedMenuIds"
        :props="{ label: 'menuName', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const roleList = ref([])
const menuTree = ref([])
const checkedMenuIds = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const isEdit = ref(false)
const roleFormRef = ref(null)
const menuTreeRef = ref(null)
const editingId = ref(null)
const currentRoleId = ref(null)

const getHeaders = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

const roleForm = reactive({
  roleName: '',
  roleCode: '',
  description: ''
})

const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

const fetchRoles = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/role/list', getHeaders())
    if (response.data.code === 200) {
      roleList.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const fetchMenus = async () => {
  try {
    const response = await axios.get('/api/menu/list', getHeaders())
    if (response.data.code === 200) {
      menuTree.value = buildTree(response.data.data)
    }
  } catch (error) {
    console.error('获取菜单列表失败')
  }
}

const buildTree = (menus) => {
  const map = {}
  const tree = []
  
  menus.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  
  menus.forEach(item => {
    if (item.parentId === 0) {
      tree.push(map[item.id])
    } else if (map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    }
  })
  
  return tree
}

const showAddDialog = () => {
  isEdit.value = false
  editingId.value = null
  Object.assign(roleForm, { roleName: '', roleCode: '', description: '' })
  dialogVisible.value = true
}

const showEditDialog = (role) => {
  isEdit.value = true
  editingId.value = role.id
  Object.assign(roleForm, { roleName: role.roleName, roleCode: role.roleCode, description: role.description })
  dialogVisible.value = true
}

const submitRole = async () => {
  try {
    await roleFormRef.value.validate()
    
    if (isEdit.value) {
      await axios.put(`/api/role/${editingId.value}`, roleForm, getHeaders())
      ElMessage.success('更新成功')
    } else {
      await axios.post('/api/role', roleForm, getHeaders())
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchRoles()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '操作失败')
  }
}

const showPermissionDialog = async (role) => {
  currentRoleId.value = role.id
  
  try {
    const response = await axios.get(`/api/role/${role.id}/menus`, getHeaders())
    if (response.data.code === 200) {
      checkedMenuIds.value = response.data.data
    }
  } catch (error) {
    checkedMenuIds.value = []
  }
  
  permDialogVisible.value = true
}

const savePermissions = async () => {
  try {
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
    const menuIds = [...checkedKeys, ...halfCheckedKeys]
    
    await axios.post(`/api/role/${currentRoleId.value}/menus`, { menuIds }, getHeaders())
    ElMessage.success('权限配置成功')
    permDialogVisible.value = false
  } catch (error) {
    ElMessage.error('权限配置失败')
  }
}

const deleteRole = async (role) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色 ${role.roleName} 吗？`, '提示')
    
    await axios.delete(`/api/role/${role.id}`, getHeaders())
    ElMessage.success('删除成功')
    fetchRoles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchRoles()
  fetchMenus()
})
</script>

<style scoped>
.role-container {
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
