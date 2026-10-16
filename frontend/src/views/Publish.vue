<template>
  <div class="container">
    <h2 class="page-title">{{ isEdit ? '编辑文章' : '发布文章' }}</h2>
    <div class="draft-tip" v-if="autoSaveTip">{{ autoSaveTip }}</div>

    <el-input v-model="form.title" placeholder="请输入文章标题" size="large" class="title-input" />

    <div class="form-row">
      <el-select v-model="form.category" placeholder="选择分类" allow-create filterable>
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="tagInput" placeholder="选择标签（可输入自定义）" multiple filterable allow-create @change="onTagsChange">
        <el-option v-for="t in hotTags" :key="t.tag" :label="t.tag" :value="t.tag" />
      </el-select>
    </div>

    <el-input v-model="form.summary" placeholder="写个摘要，让读者快速了解文章内容" />

    <div class="cover-box">
      <div class="label">封面图（可选）</div>
      <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-preview" />
      <el-button size="small" @click="$refs.coverInput.click()">{{ form.coverUrl ? '更换封面' : '上传封面' }}</el-button>
      <input ref="coverInput" type="file" accept="image/*" style="display:none" @change="uploadCover" />
    </div>

    <el-input v-model="form.content" type="textarea" :rows="15" placeholder="正文，支持Markdown语法" class="content-area" />

    <div class="upload-bar">
      <el-button size="small" @click="$refs.imgInput.click()"><el-icon><Picture /></el-icon> 插入图片</el-button>
      <input ref="imgInput" type="file" accept="image/*" style="display:none" @change="uploadImage" />
    </div>

    <div class="btn-bar">
      <el-button size="small" @click="cancel">取消</el-button>
      <el-button size="small" @click="saveDraft">存草稿</el-button>
      <el-button type="primary" :loading="loading" @click="submit">{{ isEdit ? '保存修改' : '发布文章' }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const isEdit = ref(!!route.params.id)
const categories = ['后端开发','前端开发','AI应用','数据库','架构设计','运维部署','云原生','职业成长','开源项目','其他']
const tagInput = ref([])
const hotTags = ref([])
const autoSaveTip = ref('')
const form = reactive({ title: '', category: '', tags: '', summary: '', content: '', coverUrl: '' })

const onTagsChange = (vals) => { form.tags = vals.join(',') }

let autoTimer = null
const autoSaveDraft = async () => {
  if (isEdit.value || (!form.title && !form.content)) return
  try {
    await api.post('/draft', form)
    autoSaveTip.value = '草稿已自动保存 ' + new Date().toLocaleTimeString()
  } catch (e) {}
}

onMounted(async () => {
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
    const drafts = await api.get('/draft/list')
    const d = drafts.data.find(x => x.id == route.query.draft)
    if (d) {
      form.title = d.title; form.category = d.category; form.tags = d.tags
      form.summary = d.summary; form.content = d.content; form.coverUrl = d.coverUrl || ''
      if (d.tags) tagInput.value = d.tags.split(',')
    }
  }
  autoTimer = setInterval(autoSaveDraft, 30000)
})
onUnmounted(() => { if (autoTimer) clearInterval(autoTimer) })

const saveDraft = async () => {
  await api.post('/draft', form)
  ElMessage.success('草稿已保存')
}
const cancel = () => router.back()

const uploadCover = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const fd = new FormData(); fd.append('file', file)
  const res = await api.post('/upload', fd)
  form.coverUrl = res.data.url
}
const uploadImage = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const fd = new FormData(); fd.append('file', file)
  const res = await api.post('/upload', fd)
  form.content += '\n![图片](' + res.data.url + ')\n'
  ElMessage.success('图片已插入')
  e.target.value = ''
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
  } finally { loading.value = false }
}
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 30px; background: #fff; border-radius: 8px; }
.page-title { margin: 0 0 15px; font-size: 20px; }
.draft-tip { color: #999; font-size: 12px; margin-bottom: 10px; }
.title-input { margin-bottom: 15px; }
.form-row { display: flex; gap: 10px; margin-bottom: 15px; }
.form-row > * { flex: 1; }
.label { font-size: 13px; color: #666; margin-bottom: 5px; }
.cover-box { margin-bottom: 15px; }
.cover-preview { max-width: 200px; border-radius: 6px; display: block; margin-bottom: 8px; }
.content-area { margin-bottom: 8px; }
.upload-bar { margin-bottom: 15px; }
.btn-bar { display: flex; justify-content: flex-end; gap: 10px; border-top: 1px solid #f0f0f0; padding-top: 15px; }
</style>
