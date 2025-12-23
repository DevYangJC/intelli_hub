<template>
  <div class="api-group-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">接口分组管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建分组
      </el-button>
    </div>

    <!-- 分组列表 -->
    <el-card shadow="never">
      <el-table :data="groupList" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="分组名称" min-width="150">
          <template #default="{ row }">
            <div class="group-name">
              <el-icon :style="{ color: row.color || '#409EFF' }"><Folder /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="分组编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">
            <span class="text-ellipsis">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="apiCount" label="API数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.apiCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'enabled' ? 'success' : 'info'" size="small">
              {{ row.status === 'enabled' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewGroupApis(row)">
              查看API
            </el-button>
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑分组弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分组' : '新建分组'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="分组名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分组名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="分组编码" prop="code">
          <el-input 
            v-model="formData.code" 
            placeholder="请输入分组编码，如：user-service" 
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="分组颜色">
          <el-color-picker v-model="formData.color" :predefine="predefineColors" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="formData.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入分组描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Folder } from '@element-plus/icons-vue'
import { apiGroupApi, type ApiGroupResponse } from '@/api/apiManage'

const router = useRouter()

// 状态
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

// 分组列表
const groupList = ref<ApiGroupResponse[]>([])

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  code: '',
  color: '#409EFF',
  description: '',
  enabled: true,
})

// 预设颜色
const predefineColors = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#00D1B2',
  '#7957D5',
  '#FF6B6B',
]

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入分组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入分组编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9-]*$/, message: '编码必须以小写字母开头，只能包含小写字母、数字和横线', trigger: 'blur' },
  ],
}

// 获取分组列表
const fetchGroupList = async () => {
  loading.value = true
  try {
    const res = await apiGroupApi.list()
    if (res.code === 200) {
      groupList.value = res.data || []
    }
  } catch (error) {
    console.error('获取分组列表失败:', error)
    ElMessage.error('获取分组列表失败')
  } finally {
    loading.value = false
  }
}

// 打开新建弹窗
const openCreateDialog = () => {
  isEdit.value = false
  formData.id = ''
  formData.name = ''
  formData.code = ''
  formData.color = '#409EFF'
  formData.description = ''
  formData.enabled = true
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row: ApiGroupResponse) => {
  isEdit.value = true
  formData.id = row.id
  formData.name = row.name
  formData.code = row.code || ''
  formData.color = row.color || '#409EFF'
  formData.description = row.description || ''
  formData.enabled = row.status === 'enabled'
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true

    const data = {
      name: formData.name,
      code: formData.code,
      color: formData.color,
      description: formData.description,
      status: formData.enabled ? 'enabled' : 'disabled',
    }

    if (isEdit.value) {
      await apiGroupApi.update(formData.id, data)
      ElMessage.success('分组更新成功')
    } else {
      await apiGroupApi.create(data)
      ElMessage.success('分组创建成功')
    }

    dialogVisible.value = false
    fetchGroupList()
  } catch (error: any) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

// 删除分组
const handleDelete = async (row: ApiGroupResponse) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分组 "${row.name}" 吗？删除后该分组下的API将变为未分组状态。`,
      '删除确认',
      { type: 'warning' }
    )
    
    await apiGroupApi.delete(row.id)
    ElMessage.success('分组删除成功')
    fetchGroupList()
  } catch (error: any) {
    if (error !== 'cancel' && error?.message) {
      ElMessage.error(error.message)
    }
  }
}

// 查看分组下的API
const viewGroupApis = (row: ApiGroupResponse) => {
  router.push({
    path: '/console/api/list',
    query: { groupId: row.id }
  })
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 初始化
onMounted(() => {
  fetchGroupList()
})
</script>

<style scoped>
.api-group-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.group-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.text-ellipsis {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
