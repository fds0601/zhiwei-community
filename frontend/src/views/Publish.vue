<template>
  <div class="container">
    <h2>{{ isEdit ? '编辑文章' : '发布文章' }}</h2>
    <div class="draft-tip" v-if="autoSaveTip">{{ autoSaveTip }}</div>
    <el-input v-model="form.title" placeholder="标题" style="margin-bottom:15px" />
    <el-select v-model="form.category" placeholder="选择分类" style="margin-bottom:15px;width:100%" allow-create filterable>
      <el-option label="LLM" value="LLM" />
      <el-option label="RAG" value="RAG" />
      <el-option label="Agent" value="Agent" />
      <el-option label="后端开发" value="后端开发" />
      <el-option label="前端开发" value="前端开发" />
      <el-option label="教程" value="教程" />
    </el-select>
    <el-select v-model="tagInput" placeholder="选择标签（可输入自定义）" style="margin-bottom:15px;width:100%"
      multiple filterable allow-create :reserve-keyword="false" @change="onTagsChange">
      <el-option v-for="t in hotTags" :key="t.tag" :label="t.tag" :value="t.tag" />
    </el-select>
    <el-input v-model="form.summary" placeholder="摘要" style="margin-bottom:15px" />
    <el-input v-model="form.coverUrl" placeholder="封面图URL（可选，粘贴图片链接）" style="margin-bottom:15px" />
    <el-input v-model="form.content" type="textarea" :rows="15" placeholder="正文，支持Markdown" />
    <div style="margin-top:15px;display:flex;gap:10px">
      <el-button type="primary" :loading="loading" @click="submit">
        {{ isEdit ? '保存修改' : '发布' }}
      </el-button>
      <el-button @click="saveDraft">保存草稿</el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const isEdit = ref(!!route.params.id)
const tagInput = ref([])
const hotTags = ref([])
const autoSaveTip = ref('')
const form = reactive({ title: '', category: '', tags: '', summary: '', content: '', coverUrl: '' })

const onTagsChange = (vals) => {
  form.tags = vals.join(',')
}

let autoTimer = null
const autoSaveDraft = async () => {
  if (isEdit.value || (!form.title && !form.content)) return
  try {
    await api.post('/draft', form)
    autoSaveTip.value = '草稿已自动保存 ' + new Date().toLocaleTimeString()
  } catch (e) {}
}

onMounted(async () => {
  // 加载热门标签
  try {
    const res = await api.get('/tags/hot')
    hotTags.value = res.data || []
  } catch (e) {}
  if (isEdit.value) {
    const res = await api.get('/article/' + route.params.id)
    const a = res.data.article
    form.title = a.title; form.category = a.category; form.tags = a.tags
    form.summary = a.summary; form.content = a.content; form.coverUrl = a.coverUrl || ''
    if (a.tags) tagInput.value = a.tags.split(',')
  } else if (route.query.draft) {
    // 从草稿箱进入
    const drafts = await api.get('/draft/list')
    const d = drafts.data.find(x => x.id == route.query.draft)
    if (d) {
      form.title = d.title; form.category = d.category; form.tags = d.tags
      form.summary = d.summary; form.content = d.content; form.coverUrl = d.coverUrl || ''
      if (d.tags) tagInput.value = d.tags.split(',')
    }
  }
  // 每30秒自动保存
  autoTimer = setInterval(autoSaveDraft, 30000)
})
onUnmounted(() => { if (autoTimer) clearInterval(autoTimer) })

const saveDraft = async () => {
  await api.post('/draft', form)
  ElMessage.success('草稿已保存')
}

const submit = async () => {
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.content.trim()) { ElMessage.warning('请输入正文'); return }
  loading.value = true
  try {
    if (isEdit.value) {
      await api.put('/article/' + route.params.id, form)
      ElMessage.success('修改成功')
      router.push('/article/' + route.params.id)
    } else {
      await api.post('/article', form)
      ElMessage.success('发布成功')
      router.push('/')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 30px; background: #fff; border-radius: 8px; }
.draft-tip { color: #999; font-size: 12px; margin-bottom: 10px; }
</style>
