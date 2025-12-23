<template>
  <div class="role-permission-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">角色权限</h2>
        <span class="page-desc">管理系统角色和权限配置</span>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreateRole">
          <el-icon><Plus /></el-icon>
          创建角色
        </el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <!-- 角色列表 -->
      <el-col :span="8">
        <el-card class="role-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>角色列表</span>
              <el-tag size="small">{{ roleList.length }} 个角色</el-tag>
            </div>
          </template>
          
          <div class="role-list" v-loading="roleLoading">
            <div
              v-for="role in roleList"
              :key="role.id"
              class="role-item"
              :class="{ active: currentRole?.id === role.id }"
              @click="selectRole(role)"
            >
              <div class="role-info">
                <div class="role-name">
                  {{ role.name }}
                  <el-tag v-if="role.isSystem" size="small" type="info">系统</el-tag>
                </div>
                <div class="role-code">{{ role.code }}</div>
                <div class="role-desc">{{ role.description || '暂无描述' }}</div>
              </div>
              <div class="role-actions" v-if="!role.isSystem">
                <el-button type="primary" link size="small" @click.stop="handleEditRole(role)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button type="danger" link size="small" @click.stop="handleDeleteRole(role)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <el-empty v-if="roleList.length === 0" description="暂无角色" />
          </div>
        </el-card>
      </el-col>

      <!-- 权限配置 -->
      <el-col :span="16">
        <el-card class="permission-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>权限配置</span>
              <span v-if="currentRole" class="current-role">
                当前角色：<el-tag>{{ currentRole.name }}</el-tag>
              </span>
            </div>
          </template>

          <div v-if="currentRole" class="permission-content" v-loading="permissionLoading">
            <!-- 权限分组 -->
            <div v-for="group in permissionGroups" :key="group.name" class="permission-group">
              <div class="group-header">
                <el-checkbox
                  :model-value="isGroupChecked(group)"
                  :indeterminate="isGroupIndeterminate(group)"
                  @change="(val: boolean) => toggleGroup(group, val)"
                  :disabled="currentRole.isSystem"
                >
                  {{ group.name }}
                </el-checkbox>
                <span class="group-count">
                  {{ getGroupCheckedCount(group) }}/{{ group.permissions.length }}
                </span>
              </div>
              <div class="group-permissions">
                <el-checkbox
                  v-for="perm in group.permissions"
                  :key="perm.id"
                  :model-value="selectedPermissions.includes(perm.code)"
                  @change="(val: boolean) => togglePermission(perm.code, val)"
                  :disabled="currentRole.isSystem"
                >
                  {{ perm.name }}
                </el-checkbox>
              </div>
            </div>

            <el-empty v-if="permissionGroups.length === 0" description="暂无权限配置" />

            <!-- 保存按钮 -->
            <div class="permission-footer" v-if="!currentRole.isSystem && permissionGroups.length > 0">
              <el-button type="primary" :loading="saveLoading" @click="savePermissions">
                保存权限配置
              </el-button>
              <el-button @click="resetPermissions">重置</el-button>
            </div>
            
            <el-alert
              v-if="currentRole.isSystem"
              title="系统角色的权限不可修改"
              type="info"
              :closable="false"
              show-icon
              style="margin-top: 16px;"
            />
          </div>

          <el-empty v-else description="请选择一个角色" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建/编辑角色弹窗 -->
    <el-dialog
      v-model="roleDialogVisible"
      :title="isEditRole ? '编辑角色' : '创建角色'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="roleFormRef"
        :model="roleFormData"
        :rules="roleFormRules"
        label-width="80px"
      >
        <el-form-item label="角色编码" prop="code" v-if="!isEditRole">
          <el-input v-model="roleFormData.code" placeholder="如：api_developer" />
          <div class="form-tip">角色唯一标识，创建后不可修改</div>
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleFormData.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="roleFormData.description"
            type="textarea"
            :rows="3"
            placeholder="角色描述"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="roleFormData.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitLoading" @click="handleRoleSubmit">
          {{ isEditRole ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { roleApi, type RoleResponse, type PermissionResponse } from '@/api/role'

// 状态
const roleLoading = ref(false)
const permissionLoading = ref(false)
const saveLoading = ref(false)
const roleDialogVisible = ref(false)
const isEditRole = ref(false)
const roleSubmitLoading = ref(false)

// 角色列表
const roleList = ref<RoleResponse[]>([])
const currentRole = ref<RoleResponse | null>(null)

// 权限列表
const allPermissions = ref<PermissionResponse[]>([])
const selectedPermissions = ref<string[]>([])
const originalPermissions = ref<string[]>([])

// 权限分组
const permissionGroups = computed(() => {
  const groups: { name: string; permissions: PermissionResponse[] }[] = []
  const groupMap = new Map<string, PermissionResponse[]>()
  
  allPermissions.value.forEach(perm => {
    const groupName = perm.groupName || '其他'
    if (!groupMap.has(groupName)) {
      groupMap.set(groupName, [])
    }
    groupMap.get(groupName)!.push(perm)
  })
  
  groupMap.forEach((permissions, name) => {
    groups.push({ name, permissions: permissions.sort((a, b) => a.sort - b.sort) })
  })
  
  return groups
})

// 获取角色列表
const fetchRoleList = async () => {
  roleLoading.value = true
  try {
    const res = await roleApi.list()
    roleList.value = res.data || []
    if (roleList.value.length > 0 && !currentRole.value) {
      selectRole(roleList.value[0])
    }
  } catch (error: any) {
    console.error('获取角色列表失败:', error)
    ElMessage.error(error.message || '获取角色列表失败')
  } finally {
    roleLoading.value = false
  }
}

// 获取权限列表
const fetchPermissionList = async () => {
  try {
    const res = await roleApi.listPermissions()
    allPermissions.value = res.data || []
  } catch (error: any) {
    console.error('获取权限列表失败:', error)
  }
}

// 获取角色权限
const fetchRolePermissions = async (roleId: string) => {
  permissionLoading.value = true
  try {
    const res = await roleApi.getPermissions(roleId)
    selectedPermissions.value = res.data || []
    originalPermissions.value = [...selectedPermissions.value]
  } catch (error: any) {
    console.error('获取角色权限失败:', error)
    selectedPermissions.value = []
    originalPermissions.value = []
  } finally {
    permissionLoading.value = false
  }
}

// 选择角色
const selectRole = (role: RoleResponse) => {
  currentRole.value = role
  fetchRolePermissions(role.id)
}

// 检查分组是否全选
const isGroupChecked = (group: { permissions: PermissionResponse[] }) => {
  return group.permissions.every(p => selectedPermissions.value.includes(p.code))
}

// 检查分组是否部分选中
const isGroupIndeterminate = (group: { permissions: PermissionResponse[] }) => {
  const checkedCount = group.permissions.filter(p => selectedPermissions.value.includes(p.code)).length
  return checkedCount > 0 && checkedCount < group.permissions.length
}

// 获取分组已选数量
const getGroupCheckedCount = (group: { permissions: PermissionResponse[] }) => {
  return group.permissions.filter(p => selectedPermissions.value.includes(p.code)).length
}

// 切换分组
const toggleGroup = (group: { permissions: PermissionResponse[] }, checked: boolean) => {
  if (checked) {
    group.permissions.forEach(p => {
      if (!selectedPermissions.value.includes(p.code)) {
        selectedPermissions.value.push(p.code)
      }
    })
  } else {
    group.permissions.forEach(p => {
      const index = selectedPermissions.value.indexOf(p.code)
      if (index > -1) {
        selectedPermissions.value.splice(index, 1)
      }
    })
  }
}

// 切换权限
const togglePermission = (permId: string, checked: boolean) => {
  if (checked) {
    if (!selectedPermissions.value.includes(permId)) {
      selectedPermissions.value.push(permId)
    }
  } else {
    const index = selectedPermissions.value.indexOf(permId)
    if (index > -1) {
      selectedPermissions.value.splice(index, 1)
    }
  }
}

// 保存权限
const savePermissions = async () => {
  if (!currentRole.value) return
  
  saveLoading.value = true
  try {
    await roleApi.updatePermissions(currentRole.value.id, {
      permissionIds: selectedPermissions.value,
    })
    originalPermissions.value = [...selectedPermissions.value]
    ElMessage.success('权限配置已保存')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

// 重置权限
const resetPermissions = () => {
  selectedPermissions.value = [...originalPermissions.value]
}

// 角色表单
const roleFormRef = ref<FormInstance>()
const roleFormData = reactive({
  id: '',
  code: '',
  name: '',
  description: '',
  sort: 0,
})

const roleFormRules: FormRules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '只能包含小写字母、数字和下划线，且以字母开头', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
  ],
}

// 创建角色
const handleCreateRole = () => {
  isEditRole.value = false
  roleFormData.id = ''
  roleFormData.code = ''
  roleFormData.name = ''
  roleFormData.description = ''
  roleFormData.sort = 0
  roleDialogVisible.value = true
}

// 编辑角色
const handleEditRole = (role: RoleResponse) => {
  isEditRole.value = true
  roleFormData.id = role.id
  roleFormData.code = role.code
  roleFormData.name = role.name
  roleFormData.description = role.description || ''
  roleFormData.sort = role.sort
  roleDialogVisible.value = true
}

// 删除角色
const handleDeleteRole = async (role: RoleResponse) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色"${role.name}"吗？`, '删除确认', {
      type: 'warning',
    })
    await roleApi.delete(role.id)
    ElMessage.success('角色已删除')
    if (currentRole.value?.id === role.id) {
      currentRole.value = null
    }
    fetchRoleList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 提交角色表单
const handleRoleSubmit = async () => {
  if (!roleFormRef.value) return
  
  try {
    await roleFormRef.value.validate()
    roleSubmitLoading.value = true
    
    if (isEditRole.value) {
      await roleApi.update(roleFormData.id, {
        name: roleFormData.name,
        description: roleFormData.description,
        sort: roleFormData.sort,
      })
    } else {
      await roleApi.create({
        code: roleFormData.code,
        name: roleFormData.name,
        description: roleFormData.description,
        sort: roleFormData.sort,
      })
    }
    
    ElMessage.success(isEditRole.value ? '角色更新成功' : '角色创建成功')
    roleDialogVisible.value = false
    fetchRoleList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    roleSubmitLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchRoleList()
  fetchPermissionList()
})
</script>

<style scoped>
.role-permission-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.page-desc {
  font-size: 14px;
  color: #999;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current-role {
  font-size: 14px;
  color: #666;
}

/* 角色卡片 */
.role-card {
  height: calc(100vh - 200px);
  min-height: 500px;
}

.role-card :deep(.el-card__body) {
  padding: 0;
  height: calc(100% - 60px);
  overflow-y: auto;
}

.role-list {
  padding: 8px;
}

.role-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 8px;
}

.role-item:hover {
  background: #f5f7fa;
}

.role-item.active {
  background: #ecf5ff;
  border: 1px solid #409eff;
}

.role-info {
  flex: 1;
  min-width: 0;
}

.role-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-code {
  font-size: 12px;
  color: #999;
  font-family: 'Monaco', 'Menlo', monospace;
  margin-top: 4px;
}

.role-desc {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.role-item:hover .role-actions {
  opacity: 1;
}

/* 权限卡片 */
.permission-card {
  height: calc(100vh - 200px);
  min-height: 500px;
}

.permission-card :deep(.el-card__body) {
  height: calc(100% - 60px);
  overflow-y: auto;
}

.permission-content {
  padding: 8px 0;
}

.permission-group {
  margin-bottom: 24px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 12px;
}

.group-count {
  font-size: 12px;
  color: #999;
}

.group-permissions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px 16px;
  padding: 0 12px;
}

.permission-footer {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

/* 表单提示 */
.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
