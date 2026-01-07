<template>
  <div class="templates-page">
    <!-- 顶部操作栏 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Document /></el-icon>
        Prompt 模板管理
      </h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建模板
      </el-button>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-bar">
      <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 150px">
        <el-option label="全部类型" value="" />
        <el-option label="代码" value="code" />
        <el-option label="翻译" value="translate" />
        <el-option label="写作" value="writing" />
        <el-option label="分析" value="analysis" />
        <el-option label="其他" value="other" />
      </el-select>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索模板名称..."
        clearable
        style="width: 240px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 模板列表 -->
    <div class="templates-grid">
      <div
        v-for="template in filteredTemplates"
        :key="template.id"
        class="template-card"
        @click="handlePreview(template)"
      >
        <div class="template-header">
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
        <h3 class="template-name">{{ template.name }}</h3>
        <p class="template-desc">{{ template.description || '暂无描述' }}</p>
        <div class="template-footer">
          <span class="template-code">{{ template.code }}</span>
          <span class="template-usage">使用 {{ template.usageCount || 0 }} 次</span>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="filteredTemplates.length === 0">
        <el-empty description="暂无模板">
          <el-button type="primary" @click="handleCreate">创建第一个模板</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模板' : '新建模板'"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板代码" prop="code">
          <el-input v-model="formData.code" placeholder="唯一标识，如 code_review" />
        </el-form-item>
        <el-form-item label="模板类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
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
            :rows="8"
            placeholder="使用 {变量名} 定义变量，如：请帮我翻译以下内容：{content}"
          />
          <div class="form-tip">
            使用 <code>{变量名}</code> 语法定义变量，渲染时会自动替换
          </div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="模板用途说明"
          />
        </el-form-item>
      </el-form>

      <!-- 变量预览 -->
      <div class="variables-preview" v-if="extractedVariables.length > 0">
        <h4>检测到的变量：</h4>
        <el-tag v-for="v in extractedVariables" :key="v" class="var-tag">
          {{ v }}
        </el-tag>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      :title="previewTemplate?.name"
      width="600px"
    >
      <div class="preview-content" v-if="previewTemplate">
        <div class="preview-meta">
          <el-tag :type="getTypeTagType(previewTemplate.type)" size="small">
            {{ getTypeName(previewTemplate.type) }}
          </el-tag>
          <span>代码：{{ previewTemplate.code }}</span>
          <span>使用次数：{{ previewTemplate.usageCount || 0 }}</span>
        </div>
        <div class="preview-desc">{{ previewTemplate.description }}</div>
        <div class="preview-body">
          <h4>模板内容</h4>
          <pre>{{ previewTemplate.content }}</pre>
        </div>

        <!-- 变量填写和渲染 -->
        <div class="render-section" v-if="previewVariables.length > 0">
          <h4>渲染预览</h4>
          <div class="var-inputs">
            <div v-for="v in previewVariables" :key="v" class="var-input-item">
              <label>{{ v }}：</label>
              <el-input v-model="renderValues[v]" size="small" placeholder="输入值" />
            </div>
          </div>
          <el-button type="primary" size="small" @click="handleRender" :loading="rendering">
            渲染
          </el-button>
          <div class="render-result" v-if="renderedContent">
            <h5>渲染结果</h5>
            <pre>{{ renderedContent }}</pre>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleUseTemplate" type="primary">
          使用此模板对话
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
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
  renderTemplate,
  type PromptTemplate
} from '@/api/aigc'

const router = useRouter()

// 状态
const templates = ref<PromptTemplate[]>([])
const filterType = ref('')
const searchKeyword = ref('')
const dialogVisible = ref(false)
const previewVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const rendering = ref(false)
const formRef = ref<FormInstance>()
const previewTemplate = ref<PromptTemplate | null>(null)
const renderedContent = ref('')
const renderValues = ref<Record<string, string>>({})

// 表单数据
const formData = ref<PromptTemplate>({
  name: '',
  code: '',
  type: '',
  content: '',
  description: ''
})

// 表单验证
const formRules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入模板代码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '只能包含小写字母、数字和下划线，且以字母开头', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入模板内容', trigger: 'blur' }]
}

// 提取变量
const extractVariables = (content: string): string[] => {
  const matches = content.match(/\{(\w+)\}/g)
  if (!matches) return []
  return [...new Set(matches.map(m => m.slice(1, -1)))]
}

// 计算属性
const extractedVariables = computed(() => extractVariables(formData.value.content))
const previewVariables = computed(() => previewTemplate.value ? extractVariables(previewTemplate.value.content) : [])

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

// 加载模板列表
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

// 新建模板
const handleCreate = () => {
  isEdit.value = false
  formData.value = {
    name: '',
    code: '',
    type: '',
    content: '',
    description: ''
  }
  dialogVisible.value = true
}

// 编辑模板
const handleEdit = (template: PromptTemplate) => {
  isEdit.value = true
  formData.value = { ...template }
  dialogVisible.value = true
}

// 复制模板
const handleDuplicate = (template: PromptTemplate) => {
  isEdit.value = false
  formData.value = {
    ...template,
    id: undefined,
    name: template.name + ' (副本)',
    code: template.code + '_copy'
  }
  dialogVisible.value = true
}

// 删除模板
const handleDelete = async (template: PromptTemplate) => {
  try {
    await ElMessageBox.confirm(`确定要删除模板「${template.name}」吗？`, '提示', {
      type: 'warning'
    })
    await deleteTemplate(template.id!)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch {
    // 取消
  }
}

// 下拉菜单命令
const handleCommand = (command: string, template: PromptTemplate) => {
  switch (command) {
    case 'edit':
      handleEdit(template)
      break
    case 'duplicate':
      handleDuplicate(template)
      break
    case 'delete':
      handleDelete(template)
      break
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value && formData.value.id) {
      await updateTemplate(formData.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createTemplate(formData.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadTemplates()
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    submitting.value = false
  }
}

// 预览模板
const handlePreview = (template: PromptTemplate) => {
  previewTemplate.value = template
  renderedContent.value = ''
  renderValues.value = {}
  previewVisible.value = true
}

// 渲染模板
const handleRender = async () => {
  if (!previewTemplate.value?.id) return
  
  rendering.value = true
  try {
    const res = await renderTemplate(previewTemplate.value.id, renderValues.value)
    if (res.code === 200 && res.data) {
      renderedContent.value = res.data
    }
  } catch (error) {
    ElMessage.error('渲染失败')
  } finally {
    rendering.value = false
  }
}

// 使用模板对话
const handleUseTemplate = () => {
  if (!previewTemplate.value) return
  
  // 跳转到对话页面，并带上模板内容
  let content = previewTemplate.value.content
  // 替换变量
  for (const [key, value] of Object.entries(renderValues.value)) {
    content = content.replace(new RegExp(`\\{${key}\\}`, 'g'), value || `{${key}}`)
  }
  
  router.push({
    path: '/console/aigc/chat',
    query: { prompt: encodeURIComponent(content) }
  })
}

// 搜索防抖
let searchTimer: ReturnType<typeof setTimeout>
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    // 触发过滤
  }, 300)
}

// 监听预览变量变化，重置渲染结果
watch(renderValues, () => {
  renderedContent.value = ''
}, { deep: true })

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.templates-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

/* 模板网格 */
.templates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.template-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.template-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.template-name {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.template-desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.template-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.template-code {
  font-family: monospace;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
}

/* 表单 */
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

.variables-preview {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-top: 16px;
}

.variables-preview h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #606266;
}

.var-tag {
  margin-right: 8px;
}

/* 预览 */
.preview-content {
  padding: 16px;
}

.preview-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
  color: #909399;
}

.preview-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
}

.preview-body {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.preview-body h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #303133;
}

.preview-body pre {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.render-section {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.render-section h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #303133;
}

.var-inputs {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.var-input-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.var-input-item label {
  min-width: 80px;
  font-size: 13px;
  color: #606266;
}

.render-result {
  margin-top: 12px;
  background: #ecf5ff;
  padding: 12px;
  border-radius: 8px;
}

.render-result h5 {
  margin: 0 0 8px;
  font-size: 13px;
  color: #409eff;
}

.render-result pre {
  margin: 0;
  font-size: 13px;
  color: #303133;
  white-space: pre-wrap;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 40px;
}
</style>
