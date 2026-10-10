<template>
  <div class="container">
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h2>消息通知</h2>
      <el-button link @click="readAll">全部已读</el-button>
    </div>
    <el-empty v-if="list.length===0" description="暂无通知" />
    <div v-for="n in list" :key="n.id" class="item" :class="{unread: n.isRead===0}" @click="open(n)">
      <b>{{ n.fromNickname }}</b>
      <span class="content">{{ n.content }}</span>
      <span class="time">{{ n.createTime?.substring(0,16).replace('T',' ') }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const list = ref([])
const load = async () => {
  const res = await api.get('/notification')
  list.value = res.data
}
const readAll = async () => {
  await api.post('/notification/read-all')
  load()
}
const open = (n) => {
  if (n.articleId) router.push('/article/' + n.articleId)
}
onMounted(load)
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 20px; background: #fff; border-radius: 8px; }
.item { padding: 12px; border-bottom: 1px solid #eee; display: flex; gap: 10px; cursor: pointer; }
.item.unread { background: #f0f7ff; }
.content { flex: 1; }
.time { color: #aaa; font-size: 12px; }
</style>
