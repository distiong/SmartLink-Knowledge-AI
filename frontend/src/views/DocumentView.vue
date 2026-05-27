<template>
  <div class="document-container">
    <el-card class="document-card">
      <template #header>
        <div class="card-header">
          <span>文档管理</span>
          <div class="header-actions">
            <el-select
              v-model="selectedCategory"
              placeholder="按分类筛选"
              clearable
              style="width: 150px; margin-right: 10px"
              @change="fetchDocuments"
            >
              <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.categoryName"
                :value="cat.id"
              />
            </el-select>
            <el-input
              v-model="tagSearch"
              placeholder="按标签搜索"
              clearable
              style="width: 150px; margin-right: 10px"
              @keyup.enter="fetchDocuments"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="refreshList">刷新列表</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="documents"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名称" min-width="200" />
        <el-table-column prop="fileType" label="文件类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getFileTypeTag(row.fileType)">
              {{ row.fileType?.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <span>{{ getCategoryName(row.categoryId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="150">
          <template #default="{ row }">
            <el-tag
              v-for="tag in parseTags(row.tags)"
              :key="tag"
              size="small"
              style="margin-right: 4px; margin-bottom: 4px"
            >
              {{ tag }}
            </el-tag>
            <span v-if="!row.tags" style="color: #999">无标签</span>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">
              详情
            </el-button>
            <el-button type="success" link @click="previewDocument(row)">
              预览
            </el-button>
            <el-button type="danger" link @click="deleteDocument(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && documents.length === 0" description="暂无文档" />
    </el-card>

    <el-dialog
      v-model="detailVisible"
      :title="currentDoc?.fileName"
      width="600px"
    >
      <div class="doc-detail">
        <p><strong>文件类型：</strong>{{ currentDoc?.fileType }}</p>
        <p><strong>分类：</strong>{{ getCategoryName(currentDoc?.categoryId) }}</p>
        <p><strong>标签：</strong>{{ currentDoc?.tags || '无' }}</p>
        <p><strong>版本：</strong>{{ currentDoc?.version || 1 }}</p>
        <p><strong>上传时间：</strong>{{ currentDoc?.createTime }}</p>
        <el-divider />
        <div class="doc-content">
          <h4>文档内容预览：</h4>
          <div class="content-text">{{ currentDoc?.content?.substring(0, 2000) }}...</div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="previewVisible"
      :title="'预览: ' + previewDoc?.fileName"
      width="800px"
      top="5vh"
    >
      <DocumentPreview
        v-if="previewVisible && previewDoc"
        :documentId="previewDoc.id"
        :fileType="previewDoc.fileType"
        :content="previewDoc.content"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '../utils/api'
import DocumentPreview from '../components/DocumentPreview.vue'

const documents = ref([])
const categories = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const previewVisible = ref(false)
const currentDoc = ref(null)
const previewDoc = ref(null)
const selectedCategory = ref('')
const tagSearch = ref('')

onMounted(() => {
  fetchCategories()
  fetchDocuments()
})

const fetchCategories = async () => {
  try {
    const res = await api.get('/category/list')
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败', error)
  }
}

const fetchDocuments = async () => {
  loading.value = true
  try {
    let url = '/document/list'
    const params = {}
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }
    if (tagSearch.value) {
      params.tag = tagSearch.value
    }
    const res = await api.get(url, { params })
    documents.value = res.data || []
  } catch (error) {
    ElMessage.error('获取文档列表失败')
  } finally {
    loading.value = false
  }
}

const refreshList = () => {
  selectedCategory.value = ''
  tagSearch.value = ''
  fetchDocuments()
}

const getCategoryName = (categoryId) => {
  if (!categoryId) return '未分类'
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.categoryName : '未分类'
}

const parseTags = (tags) => {
  if (!tags) return []
  return tags.split(',').map(t => t.trim()).filter(t => t)
}

const getFileTypeTag = (type) => {
  const map = {
    pdf: 'danger',
    docx: 'primary',
    txt: 'info',
    md: 'success',
    csv: 'warning',
    html: ''
  }
  return map[type?.toLowerCase()] || ''
}

const viewDetail = (doc) => {
  currentDoc.value = doc
  detailVisible.value = true
}

const previewDocument = (doc) => {
  previewDoc.value = doc
  previewVisible.value = true
}

const deleteDocument = async (doc) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档 "${doc.fileName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await api.delete('/document/delete', {
      params: { id: doc.id }
    })

    ElMessage.success('文档删除成功')
    fetchDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('文档删除失败')
    }
  }
}
</script>

<style scoped>
.document-container {
  max-width: 1200px;
  margin: 0 auto;
}

.document-card {
  min-height: calc(100vh - 140px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.doc-detail p {
  margin-bottom: 8px;
}

.doc-content {
  margin-top: 16px;
}

.doc-content h4 {
  margin-bottom: 12px;
  color: #303133;
}

.content-text {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
  line-height: 1.6;
  font-size: 14px;
}
</style>