<template>
  <div class="container">
    <h2>{{ isEdit ? '编辑文章' : '发布文章' }}</h2>
    <el-input v-model="form.title" placeholder="标题" style="margin-bottom:15px" />
    <el-input v-model="form.category" placeholder="分类（如RAG/Agent/LLM/教程）" style="margin-bottom:15px" />
    <el-input v-model="form.tags" placeholder="标签，逗号分隔（如RAG,向量数据库）" style="margin-bottom:15px" />
    <el-input v-model="form.summary" placeholder="摘要" style="margin-bottom:15px" />
    <el-input v-model="form.coverUrl" placeholder="封面图URL（可选，粘贴图片链接）" style="margin-bottom:15px" />
    <el-input v-model="form.content" type="textarea" :rows="15" placeholder="正文，支持Markdown" />
    <el-button type="primary" :loading="loading" @click="submit" style="margin-top:15px">
      {{ isEdit ? '保存修改' : '发布' }}
    </el-button>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const isEdit = ref(!!route.params.id)
const form = reactive({ title: '', category: '', tags: '', summary: '', content: '', coverUrl: '' })

onMounted(async () => {
  if (isEdit.value) {
    const res = await api.get('/article/' + route.params.id)
    const a = res.data.article
    form.title = a.title
    form.category = a.category
    form.tags = a.tags
    form.summary = a.summary
    form.content = a.content
    form.coverUrl = a.coverUrl || ''
  }
})

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
</style>
