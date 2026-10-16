<template>
  <div class="home-layout">
    <div class="sidebar">
      <div class="side-card">
        <div v-for="c in categories" :key="c.name" class="side-item" :class="{active: category===c.name}" @click="category=c.name; loadList()">
          <el-icon :size="16" style="vertical-align:middle;margin-right:6px"><component :is="c.icon" /></el-icon> {{ c.name || '全部' }}
        </div>
      </div>
    </div>
    <div class="main-content">
      <el-empty v-if="list.length === 0" description="暂无AI工具" />
      <div v-for="t in list" :key="t.id" class="feed-item" @click="openUrl(t.url)">
        <div class="feed-info">
          <h3 class="feed-title">{{ t.name }}</h3>
          <p class="feed-summary">{{ t.description }}</p>
          <div class="feed-meta">
            <el-tag size="small">{{ t.category }}</el-tag>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialog" title="提交AI工具" width="500px">
      <el-input v-model="form.name" placeholder="工具名称" style="margin-bottom:10px" />
      <el-input v-model="form.url" placeholder="官网地址 https://..." style="margin-bottom:10px" />
      <el-select v-model="form.category" placeholder="选择分类" style="margin-bottom:10px;width:100%">
        <el-option v-for="c in categories.filter(x=>x.name)" :key="c.name" :label="c.name" :value="c.name" />
      </el-select>
      <el-input v-model="form.description" type="textarea" :rows="3" placeholder="工具简介" />
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Odometer, Platform, Box, Cpu, Monitor, Coin, Document } from '@element-plus/icons-vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const token = ref(localStorage.getItem('token'))
const list = ref([])
const category = ref('')
const categories = [
  { name: '', icon: Odometer },
  { name: 'AI开发平台', icon: Platform },
  { name: '开发框架', icon: Box },
  { name: '模型部署', icon: Cpu },
  { name: 'AI客户端', icon: Monitor },
  { name: '向量数据库', icon: Coin },
  { name: 'API管理', icon: Document }
]
const dialog = ref(false)
const form = reactive({ name: '', url: '', category: '', description: '' })

const loadList = async () => {
  const res = await api.get('/tool/list', { params: { page: 1, size: 50, category: category.value } })
  list.value = res.data.records
}
const openUrl = (url) => window.open(url, '_blank')
const submit = async () => {
  try {
    await api.post('/tool', form)
    ElMessage.success('提交成功')
    dialog.value = false
    form.name = form.url = form.category = form.description = ''
    loadList()
  } catch (e) {}
}
onMounted(() => {
  loadList()
  if (route.query.submit) {
    dialog.value = true
    history.replaceState(null, '', '/tools')
  }
  window.addEventListener('open-tool-dialog', () => {
    if (!localStorage.getItem('token')) {
      ElMessage.warning('请先登录')
      setTimeout(() => router.push('/login'), 500)
      return
    }
    dialog.value = true
  })
})
</script>

<style scoped>
.home-layout { display: flex; gap: 16px; max-width: 1100px; margin: 20px auto; padding: 0 20px; }
.sidebar { width: 200px; flex-shrink: 0; }
.side-card { background: #fff; border-radius: 8px; padding: 12px 0; }
.side-item { padding: 10px 16px; cursor: pointer; color: #555; }
.side-item:hover { background: #f5f7fa; }
.side-item.active { background: #e8f3ff; color: #1e80ff; }
.main-content { flex: 1; background: #fff; border-radius: 8px; padding: 20px; }
.feed-item { padding: 16px 0; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.feed-item:last-child { border-bottom: none; }
.feed-title { margin: 0 0 6px; font-size: 16px; }
.feed-summary { color: #666; font-size: 13px; margin: 0 0 8px; }
.feed-meta { display: flex; gap: 8px; }
</style>
