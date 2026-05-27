<template>
  <div class="category-container">
    <el-card class="category-card">
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <el-button type="primary" @click="showAddDialog">新增分类</el-button>
        </div>
      </template>

      <el-table
        :data="categories"
        v-loading="loading"
        stripe
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        style="width: 100%"
      >
        <el-table-column prop="categoryName" label="分类名称" min-width="200" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button type="primary" link @click="showAddChildDialog(row)">
              添加子分类
            </el-button>
            <el-button type="danger" link @click="deleteCategory(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && categories.length === 0" description="暂无分类" />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="500px"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="分类名称" required>
          <el-input v-model="formData.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="formData.parentId" placeholder="请选择父分类（可选）" clearable style="width: 100%">
            <el-option label="无（顶级分类）" :value="0" />
            <el-option
              v-for="cat in parentCategories"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editingId = ref(null)

const formData = ref({
  categoryName: '',
  parentId: 0,
  sort: 0
})

const parentCategories = computed(() => {
  return categories.value.filter(cat => cat.parentId === 0 || cat.parentId === null)
})

onMounted(() => {
  fetchCategories()
})

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await api.get('/category/list')
    categories.value = buildTree(res.data || [])
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

const buildTree = (list) => {
  const map = {}
  const roots = []

  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })

  list.forEach(item => {
    if (item.parentId && item.parentId !== 0 && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })

  return roots
}

const showAddDialog = () => {
  isEdit.value = false
  editingId.value = null
  formData.value = {
    categoryName: '',
    parentId: 0,
    sort: 0
  }
  dialogVisible.value = true
}

const showAddChildDialog = (parent) => {
  isEdit.value = false
  editingId.value = null
  formData.value = {
    categoryName: '',
    parentId: parent.id,
    sort: 0
  }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editingId.value = row.id
  formData.value = {
    categoryName: row.categoryName,
    parentId: row.parentId || 0,
    sort: row.sort || 0
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formData.value.categoryName.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await api.put(`/category/${editingId.value}`, formData.value)
      ElMessage.success('分类更新成功')
    } else {
      await api.post('/category', formData.value)
      ElMessage.success('分类创建成功')
    }
    dialogVisible.value = false
    fetchCategories()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

const deleteCategory = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类 "${row.categoryName}" 吗？删除后该分类下的文档将变为未分类。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await api.delete(`/category/${row.id}`)
    ElMessage.success('分类删除成功')
    fetchCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('分类删除失败')
    }
  }
}
</script>

<style scoped>
.category-container {
  max-width: 1200px;
  margin: 0 auto;
}

.category-card {
  min-height: calc(100vh - 140px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
