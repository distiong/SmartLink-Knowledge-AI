<template>
  <div class="graph-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>图谱数据录入</span>
              <div class="header-actions">
                <el-dropdown @command="exportGraph">
                  <el-button type="primary" size="small">
                    导出 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="json">导出JSON</el-dropdown-item>
                      <el-dropdown-item command="csv">导出CSV</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-upload
                  :show-file-list="false"
                  :before-upload="beforeImport"
                  :http-request="importGraph"
                  accept=".json,.csv"
                >
                  <el-button type="success" size="small">导入</el-button>
                </el-upload>
              </div>
            </div>
          </template>

          <el-form :model="relationForm" label-width="80px">
            <el-form-item label="主体">
              <el-input v-model="relationForm.subject" placeholder="请输入主体实体" />
            </el-form-item>
            <el-form-item label="关系">
              <el-input v-model="relationForm.relation" placeholder="请输入关系名称" />
            </el-form-item>
            <el-form-item label="客体">
              <el-input v-model="relationForm.object" placeholder="请输入客体实体" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="createRelation" :loading="creating" style="width:100%">
                创建关系
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <div class="query-section">
            <h4>图谱查询</h4>
            <el-input
              v-model="queryEntity"
              placeholder="请输入实体名称查询"
              @keyup.enter="queryGraph"
            >
              <template #append>
                <el-button :loading="querying" @click="queryGraph">查询</el-button>
              </template>
            </el-input>
          </div>

          <div class="reasoning-section">
            <h4>多跳推理</h4>
            <el-input
              v-model="reasoningEntity"
              placeholder="请输入起始实体"
              @keyup.enter="multiHopReasoning"
            >
              <template #prepend>跳数</template>
              <template #append>
                <el-select v-model="hopCount" style="width: 80px">
                  <el-option :value="2" label="2跳" />
                  <el-option :value="3" label="3跳" />
                  <el-option :value="4" label="4跳" />
                </el-select>
              </template>
            </el-input>
            <el-button
              type="warning"
              @click="multiHopReasoning"
              :loading="reasoning"
              style="width: 100%; margin-top: 10px"
            >
              开始推理
            </el-button>
          </div>

          <div v-if="queryResult.relations.length > 0" class="relations-list">
            <el-divider content-position="left">关系列表</el-divider>
            <div v-for="(rel, index) in queryResult.relations" :key="index" class="relation-item">
              <el-tag>{{ rel.subject }}</el-tag>
              <span class="relation-arrow">→</span>
              <el-tag type="warning">{{ rel.relation }}</el-tag>
              <span class="relation-arrow">→</span>
              <el-tag type="success">{{ rel.object }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="graph-card">
          <template #header>
            <span>知识图谱可视化</span>
          </template>

          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'

const chartRef = ref(null)
let chart = null

const relationForm = reactive({
  subject: '',
  relation: '',
  object: ''
})

const queryEntity = ref('')
const reasoningEntity = ref('')
const hopCount = ref(2)
const creating = ref(false)
const querying = ref(false)
const reasoning = ref(false)

const queryResult = reactive({
  nodes: [],
  edges: [],
  relations: []
})

const getHeaders = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

onMounted(() => {
  nextTick(() => {
    if (chartRef.value) {
      chart = echarts.init(chartRef.value)
      updateChart()
      window.addEventListener('resize', () => chart?.resize())
    }
  })
})

onUnmounted(() => {
  chart?.dispose()
  window.removeEventListener('resize', () => chart?.resize())
})

const updateChart = () => {
  if (!chart) return

  const nodes = queryResult.nodes.map(node => ({
    id: String(node.id),
    name: node.name,
    symbolSize: 50,
    itemStyle: {
      color: '#409eff'
    }
  }))

  const links = queryResult.edges.map(edge => ({
    source: String(edge.source),
    target: String(edge.target),
    label: {
      show: true,
      formatter: edge.label,
      fontSize: 12
    }
  }))

  const option = {
    tooltip: {},
    series: [
      {
        type: 'graph',
        layout: 'force',
        data: nodes,
        links: links,
        roam: true,
        label: {
          show: true,
          position: 'right'
        },
        force: {
          repulsion: 200,
          edgeLength: 150
        },
        lineStyle: {
          color: '#999',
          curveness: 0.3
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 4
          }
        }
      }
    ]
  }

  chart.setOption(option, true)
}

const createRelation = async () => {
  if (!relationForm.subject || !relationForm.relation || !relationForm.object) {
    ElMessage.warning('请填写完整的关系信息')
    return
  }

  creating.value = true
  try {
    await axios.post('/api/graph/create', relationForm, getHeaders())
    ElMessage.success('关系创建成功')
    relationForm.subject = ''
    relationForm.relation = ''
    relationForm.object = ''

    if (queryEntity.value) {
      queryGraph()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '关系创建失败')
  } finally {
    creating.value = false
  }
}

const queryGraph = async () => {
  if (!queryEntity.value.trim()) {
    ElMessage.warning('请输入查询实体')
    return
  }

  querying.value = true
  try {
    const res = await axios.get('/api/graph/query', {
      params: { entity: queryEntity.value.trim() },
      ...getHeaders()
    })

    if (res.data.code === 200) {
      queryResult.nodes = res.data.data.nodes || []
      queryResult.edges = res.data.data.edges || []
      queryResult.relations = res.data.data.relations || []
      updateChart()

      if (queryResult.relations.length === 0) {
        ElMessage.info('未找到相关关系')
      }
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '查询失败')
  } finally {
    querying.value = false
  }
}

const multiHopReasoning = async () => {
  if (!reasoningEntity.value.trim()) {
    ElMessage.warning('请输入起始实体')
    return
  }

  reasoning.value = true
  try {
    const res = await axios.get('/api/graph/reasoning', {
      params: {
        entity: reasoningEntity.value.trim(),
        hops: hopCount.value
      },
      ...getHeaders()
    })

    if (res.data.code === 200) {
      queryResult.nodes = res.data.data.nodes || []
      queryResult.edges = res.data.data.edges || []
      queryResult.relations = res.data.data.relations || []
      updateChart()

      if (queryResult.relations.length === 0) {
        ElMessage.info('未找到相关关系')
      } else {
        ElMessage.success(`找到 ${queryResult.relations.length} 条关系链`)
      }
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '推理失败')
  } finally {
    reasoning.value = false
  }
}

const exportGraph = async (format) => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`/api/graph/export/${format}`, {
      headers: { Authorization: `Bearer ${token}` },
      responseType: 'blob'
    })

    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `graph_data.${format}`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const beforeImport = (file) => {
  const isJson = file.type === 'application/json' || file.name.endsWith('.json')
  const isCsv = file.name.endsWith('.csv')

  if (!isJson && !isCsv) {
    ElMessage.error('只能上传 JSON 或 CSV 文件')
    return false
  }

  return true
}

const importGraph = async (options) => {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)

  const format = file.name.endsWith('.json') ? 'json' : 'csv'

  try {
    const token = localStorage.getItem('token')
    await axios.post(`/api/graph/import/${format}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      }
    })

    ElMessage.success('导入成功')
    queryGraph()
  } catch (error) {
    ElMessage.error('导入失败')
  }
}
</script>

<style scoped>
.graph-container {
  height: calc(100vh - 140px);
}

.form-card,
.graph-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.query-section,
.reasoning-section {
  margin-top: 20px;
}

.query-section h4,
.reasoning-section h4 {
  margin-bottom: 12px;
  color: #303133;
}

.relations-list {
  margin-top: 20px;
}

.relation-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.relation-arrow {
  color: #909399;
}

.chart-container {
  width: 100%;
  height: 500px;
}
</style>
