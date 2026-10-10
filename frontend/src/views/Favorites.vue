<template>
  <div class="container">
    <h2>我的收藏</h2>
    <el-empty v-if="list.length === 0" description="还没有收藏任何文章" />
    <ArticleCard v-for="a in list" :key="a.id" :article="a" />
    <el-pagination v-if="total > size" :current-page="page" :page-size="size"
      :total="total" layout="prev, pager, next" @current-change="load" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import ArticleCard from '../components/ArticleCard.vue'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const load = async () => {
  const res = await api.get('/article/favorites', { params: { page: page.value, size: size.value } })
  list.value = res.data.records || []
  total.value = res.data.total
}
onMounted(load)
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 0 20px; }
</style>
