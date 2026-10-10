<template>
  <div class="container">
    <h2>我的草稿</h2>
    <el-empty v-if="list.length === 0" description="还没有草稿，写文章时会自动保存" />
    <div v-for="d in list" :key="d.id" class="draft-item">
      <div class="draft-info" @click="$router.push('/publish?draft=' + d.id)">
        <div class="draft-title">{{ d.title || '无标题' }}</div>
        <div class="draft-meta">更新于 {{ d.updateTime?.substring(0,16) }}</div>
      </div>
      <div class="draft-actions">
        <el-button size="small" @click="$router.push('/publish?draft=' + d.id)">编辑</el-button>
        <el-button size="small" type="danger" @click="remove(d.id)">删除</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const list = ref([])
const load = async () => {
  const res = await api.get('/draft/list')
  list.value = res.data || []
}
const remove = async (id) => {
  await api.delete('/draft/' + id)
  load()
}
onMounted(load)
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 0 20px; }
.draft-item { display: flex; justify-content: space-between; align-items: center; padding: 16px; border-bottom: 1px solid #eee; }
.draft-title { font-size: 16px; font-weight: 500; }
.draft-meta { font-size: 12px; color: #999; margin-top: 4px; }
</style>
