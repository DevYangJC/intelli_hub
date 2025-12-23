<template>
  <div class="param-table">
    <!-- 提示信息 -->
    <el-alert v-if="paramType === 'path'" type="info" :closable="false" class="param-tip">
      <template #title>
        Path参数从请求路径中提取，如 <code>/users/{userId}</code> 中的 <code>userId</code>
      </template>
    </el-alert>
    <el-alert v-else-if="paramType === 'header'" type="info" :closable="false" class="param-tip">
      <template #title>
        常用Header：<code>Authorization</code>、<code>X-Request-Id</code>、<code>X-Tenant-Id</code>
      </template>
    </el-alert>

    <el-table :data="modelValue" style="width: 100%" size="small">
      <!-- 参数名 -->
      <el-table-column label="参数名" width="180">
        <template #default="{ row }">
          <el-select
            v-if="paramType === 'header'"
            v-model="row.name"
            filterable
            allow-create
            placeholder="选择或输入"
            size="small"
            @change="emitChange"
          >
            <el-option label="Authorization" value="Authorization" />
            <el-option label="Content-Type" value="Content-Type" />
            <el-option label="X-Request-Id" value="X-Request-Id" />
            <el-option label="X-Tenant-Id" value="X-Tenant-Id" />
            <el-option label="X-Api-Key" value="X-Api-Key" />
          </el-select>
          <el-input v-else v-model="row.name" :placeholder="getNamePlaceholder" size="small" @change="emitChange" />
        </template>
      </el-table-column>

      <!-- 类型 -->
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          <el-select v-model="row.type" placeholder="类型" size="small" @change="emitChange">
            <el-option label="string" value="string" />
            <el-option label="integer" value="integer" />
            <el-option label="number" value="number" v-if="paramType !== 'header'" />
            <el-option label="boolean" value="boolean" v-if="paramType === 'query'" />
            <el-option label="array" value="array" v-if="paramType === 'query' || paramType === 'body'" />
            <el-option label="object" value="object" v-if="paramType === 'body'" />
          </el-select>
        </template>
      </el-table-column>

      <!-- 必填 -->
      <el-table-column label="必填" width="70" align="center">
        <template #default="{ row }">
          <el-checkbox v-model="row.required" @change="emitChange" :disabled="paramType === 'path'" />
        </template>
      </el-table-column>

      <!-- 默认值（仅Query参数） -->
      <el-table-column v-if="paramType === 'query'" label="默认值" width="120">
        <template #default="{ row }">
          <el-input v-model="row.defaultValue" placeholder="默认值" size="small" @change="emitChange" />
        </template>
      </el-table-column>

      <!-- 描述 -->
      <el-table-column label="描述" min-width="150">
        <template #default="{ row }">
          <el-input v-model="row.description" placeholder="参数描述" size="small" @change="emitChange" />
        </template>
      </el-table-column>

      <!-- 示例值 -->
      <el-table-column label="示例值" width="120">
        <template #default="{ row }">
          <el-input v-model="row.example" placeholder="示例" size="small" @change="emitChange" />
        </template>
      </el-table-column>

      <!-- 操作 -->
      <el-table-column label="操作" width="80" align="center">
        <template #default="{ $index }">
          <el-button type="danger" link size="small" @click="removeRow($index)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-button class="add-btn" text type="primary" @click="addRow">
      <el-icon><Plus /></el-icon>
      {{ addButtonText }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Delete, Plus } from '@element-plus/icons-vue'

interface Param {
  name: string
  type: string
  required: boolean
  description: string
  example: string
  defaultValue?: string
}

const props = defineProps<{
  modelValue: Param[]
  paramType: 'query' | 'path' | 'header' | 'body'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: Param[]]
}>()

// 根据参数类型显示不同的占位符
const getNamePlaceholder = computed(() => {
  switch (props.paramType) {
    case 'path':
      return '如: userId'
    case 'query':
      return '如: page, size'
    case 'header':
      return '如: Authorization'
    default:
      return '参数名'
  }
})

// 根据参数类型显示不同的按钮文字
const addButtonText = computed(() => {
  switch (props.paramType) {
    case 'path':
      return '添加Path参数'
    case 'query':
      return '添加Query参数'
    case 'header':
      return '添加Header'
    case 'body':
      return '添加Body字段'
    default:
      return '添加参数'
  }
})

const emitChange = () => {
  emit('update:modelValue', [...props.modelValue])
}

const addRow = () => {
  const newParam: Param = {
    name: '',
    type: 'string',
    required: props.paramType === 'path', // Path参数默认必填
    description: '',
    example: '',
    defaultValue: '',
  }
  emit('update:modelValue', [...props.modelValue, newParam])
}

const removeRow = (index: number) => {
  const newList = [...props.modelValue]
  newList.splice(index, 1)
  emit('update:modelValue', newList)
}
</script>

<style scoped>
.param-table {
  width: 100%;
}

.param-tip {
  margin-bottom: 12px;
}

.param-tip code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
}

.add-btn {
  margin-top: 12px;
}
</style>
