<template>
  <div class="announcements-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">公告管理</h1>
        <p class="page-desc">管理首页的最新动态和系统公告</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增公告</el-button>
    </div>

    <!-- 公告列表 -->
    <el-card>
      <el-table :data="announcements" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'published' ? 'success' : 'info'" size="small">
              {{ row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              :type="row.status === 'published' ? 'warning' : 'success'" 
              link 
              size="small" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'published' ? '下线' : '发布' }}
            </el-button>
            <el-popconfirm title="确定删除该公告？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑公告' : '新增公告'"
      width="600px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="系统通知" value="notice" />
            <el-option label="功能更新" value="feature" />
            <el-option label="维护公告" value="maintenance" />
            <el-option label="重要提醒" value="warning" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入公告内容" 
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.meta" placeholder="可选，如：重要、功能更新等" />
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="form.publishTime"
            type="datetime"
            placeholder="选择发布时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getAnnouncementList, 
  createAnnouncement, 
  updateAnnouncement, 
  publishAnnouncement, 
  unpublishAnnouncement, 
  deleteAnnouncement,
  type AnnouncementDTO 
} from '@/api/announcement'

interface Announcement {
  id: string
  title: string
  description: string
  type: string
  meta: string
  status: 'published' | 'draft'
  publishTime: string
}

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

// 公告列表
const announcements = ref<Announcement[]>([])

// 表单数据
const form = reactive<Partial<Announcement>>({
  title: '',
  description: '',
  type: 'notice',
  meta: '',
  publishTime: ''
})

// 表单验证规则
const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 获取类型标签颜色
const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    notice: 'info',
    feature: 'success',
    maintenance: 'warning',
    warning: 'danger'
  }
  return map[type] || 'info'
}

// 获取类型名称
const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    notice: '系统通知',
    feature: '功能更新',
    maintenance: '维护公告',
    warning: '重要提醒'
  }
  return map[type] || type
}

// 新增公告
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: '',
    title: '',
    description: '',
    type: 'notice',
    meta: '',
    publishTime: new Date().toISOString().slice(0, 16).replace('T', ' ')
  })
  dialogVisible.value = true
}

// 编辑公告
const handleEdit = (row: Announcement) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList({ page: 1, size: 50 })
    if (res.code === 200 && res.data) {
      announcements.value = (res.data.records || []).map((item: AnnouncementDTO) => ({
        id: item.id || '',
        title: item.title,
        description: item.description,
        type: item.type,
        meta: item.meta || '',
        status: item.status as 'published' | 'draft',
        publishTime: item.publishTime || ''
      }))
    }
  } catch (error) {
    console.error('加载公告失败', error)
  } finally {
    loading.value = false
  }
}

// 切换发布状态
const handleToggleStatus = async (row: Announcement) => {
  try {
    if (row.status === 'published') {
      await unpublishAnnouncement(row.id)
      row.status = 'draft'
      ElMessage.success('已下线')
    } else {
      await publishAnnouncement(row.id)
      row.status = 'published'
      ElMessage.success('已发布')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除公告
const handleDelete = async (row: Announcement) => {
  try {
    await deleteAnnouncement(row.id)
    const index = announcements.value.findIndex(a => a.id === row.id)
    if (index > -1) {
      announcements.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 保存公告
const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  
  try {
    const data: AnnouncementDTO = {
      title: form.title || '',
      description: form.description || '',
      type: form.type || 'notice',
      meta: form.meta,
      publishTime: form.publishTime
    }
    
    if (isEdit.value && form.id) {
      await updateAnnouncement(form.id, data)
      ElMessage.success('更新成功')
    } else {
      await createAnnouncement(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadAnnouncements()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcements-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.page-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
}
</style>
