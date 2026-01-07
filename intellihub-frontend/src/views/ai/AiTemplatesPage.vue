<template>
  <div class="ai-templates-page">
    <div class="page-container">
      <!-- 顶部 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon><Document /></el-icon>
            Prompt 模板
          </h1>
          <p class="page-desc">管理和使用 AI 提示词模板，提高对话效率</p>
        </div>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建模板
        </el-button>
      </div>

      <!-- 筛选 -->
      <div class="filter-bar">
        <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 140px">
          <el-option label="全部类型" value="" />
          <el-option label="代码" value="code" />
          <el-option label="翻译" value="translate" />
          <el-option label="写作" value="writing" />
          <el-option label="分析" value="analysis" />
          <el-option label="其他" value="other" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索模板..."
          clearable
          style="width: 220px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 模板网格 -->
      <div class="templates-grid">
        <div
          v-for="template in filteredTemplates"
          :key="template.id"
          class="template-card"
        >
          <div class="card-header">
            <el-tag :type="getTypeTagType(template.type)" size="small">
              {{ getTypeName(template.type) }}
            </el-tag>
            <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, template)">
              <el-button text size="small" @click.stop>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="duplicate">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <h3 class="card-title">{{ template.name }}</h3>
          <p class="card-desc">{{ template.description || '暂无描述' }}</p>
          <div class="card-footer">
            <span class="template-code">{{ template.code }}</span>
            <span class="template-usage">{{ template.usageCount || 0 }} 次使用</span>
          </div>
          <div class="card-actions">
            <el-button type="primary" size="small" @click="handleUse(template)">
              使用模板
            </el-button>
          </div>
        </div>

        <el-empty v-if="filteredTemplates.length === 0" description="暂无模板" />
      </div>
    </div>

    <!-- 创建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模板' : '新建模板'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="formData.name" placeholder="如：代码审查助手" />
        </el-form-item>
        <el-form-item label="模板代码" prop="code">
          <el-input v-model="formData.code" placeholder="唯一标识，如 code_review" />
        </el-form-item>
        <el-form-item label="模板类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择" style="width: 100%">
            <el-option label="代码" value="code" />
            <el-option label="翻译" value="translate" />
            <el-option label="写作" value="writing" />
            <el-option label="分析" value="analysis" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            placeholder="使用 {变量名} 定义变量"
          />
          <div class="form-tip">
            提示：使用 <code>{变量名}</code> 语法定义变量
          </div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="模板用途说明" />
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document,
  Plus,
  Search,
  MoreFilled,
  Edit,
  CopyDocument,
  Delete
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getTemplateList,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  type PromptTemplate
} from '@/api/aigc'

const router = useRouter()

// 状态
const templates = ref<PromptTemplate[]>([])
const filterType = ref('')
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const formData = ref<PromptTemplate>({
  name: '',
  code: '',
  type: '',
  content: '',
  description: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入模板代码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '只能包含小写字母、数字和下划线', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入模板内容', trigger: 'blur' }]
}

// 计算属性
const filteredTemplates = computed(() => {
  let result = templates.value
  if (filterType.value) {
    result = result.filter(t => t.type === filterType.value)
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t =>
      t.name.toLowerCase().includes(keyword) ||
      t.code.toLowerCase().includes(keyword)
    )
  }
  return result
})

// 类型映射
const typeNames: Record<string, string> = {
  code: '代码',
  translate: '翻译',
  writing: '写作',
  analysis: '分析',
  other: '其他'
}

const typeTagTypes: Record<string, string> = {
  code: 'primary',
  translate: 'success',
  writing: 'warning',
  analysis: 'info',
  other: ''
}

const getTypeName = (type: string) => typeNames[type] || type
const getTypeTagType = (type: string) => typeTagTypes[type] || ''

// 加载模板
const loadTemplates = async () => {
  try {
    const res = await getTemplateList()
    if (res.code === 200 && res.data) {
      templates.value = res.data
    }
  } catch (error) {
    console.error('加载模板失败', error)
  }
}

// 新建
const handleCreate = () => {
  isEdit.value = false
  formData.value = { name: '', code: '', type: '', content: '', description: '' }
  dialogVisible.value = true
}

// 命令处理
const handleCommand = async (command: string, template: PromptTemplate) => {
  if (command === 'edit') {
    isEdit.value = true
    formData.value = { ...template }
    dialogVisible.value = true
  } else if (command === 'duplicate') {
    isEdit.value = false
    formData.value = { ...template, id: undefined, name: template.name + ' (副本)', code: template.code + '_copy' }
    dialogVisible.value = true
  } else if (command === 'delete') {
    await ElMessageBox.confirm(`确定删除「${template.name}」？`, '提示', { type: 'warning' })
    await deleteTemplate(template.id!)
    ElMessage.success('删除成功')
    loadTemplates()
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value && formData.value.id) {
      await updateTemplate(formData.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createTemplate(formData.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTemplates()
  } finally {
    submitting.value = false
  }
}

// 使用模板
const handleUse = (template: PromptTemplate) => {
  router.push({ path: '/ai/chat', query: { template: template.id } })
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.ai-templates-page {
  min-height: calc(100vh - 56px);
  background: #f5f7fa;
  padding: 24px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
}

.page-desc {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.templates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.template-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #ebeef5;
  transition: all 0.2s;
}

.template-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.template-code {
  font-family: monospace;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.form-tip code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  color: #e83e8c;
}
</style>
