<template>
  <div class="container">
    <div class="profile-header">
      <div>
        <h2>{{ nickname }}</h2>
        <p class="bio">{{ bio || '这个人很懒，什么都没写' }}</p>
        <div class="stats">
          <span>{{ stats.articleCount || 0 }} 文章</span>
          <span>{{ stats.likeReceived || 0 }} 获赞</span>
          <span>{{ followerCount || 0 }} 粉丝</span>
          <span>{{ followCount || 0 }} 关注</span>
        </div>
      </div>
      <el-button v-if="isSelf" @click="showEdit = true">编辑资料</el-button>
      <el-button v-else :type="followed ? 'default' : 'primary'" @click="doFollow">
        {{ followed ? '已关注' : '关注' }}
      </el-button>
    </div>
    <el-empty v-if="list.length === 0" description="还没有发布文章" />
    <ArticleCard v-for="a in list" :key="a.id" :article="a" />

    <el-dialog v-model="showEdit" title="编辑资料" width="400px">
      <el-input v-model="editForm.nickname" placeholder="昵称" style="margin-bottom:10px" />
      <el-input v-model="editForm.bio" type="textarea" placeholder="简介" />
      <el-button type="primary" @click="saveProfile" style="margin-top:10px">保存</el-button>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import ArticleCard from '../components/ArticleCard.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const list = ref([])
const nickname = ref('用户')
const bio = ref('')
const stats = ref({})
const followerCount = ref(0)
const followCount = ref(0)
const followed = ref(false)
const showEdit = ref(false)
const editForm = ref({ nickname: '', bio: '' })
const myUserId = ref(localStorage.getItem('userId'))
const isSelf = computed(() => myUserId.value == route.params.id)

const loadData = async () => {
  const uid = route.params.id
  const [artRes, userRes, statRes] = await Promise.all([
    api.get(`/article/user/${uid}`, { params: { page: 1, size: 50 } }),
    api.get(`/user/${uid}`),
    api.get(`/article/user/${uid}/stats`)
  ])
  list.value = artRes.data.records || []
  nickname.value = userRes.data.nickname
  bio.value = userRes.data.bio
  followerCount.value = userRes.data.followerCount
  followCount.value = userRes.data.followCount
  followed.value = userRes.data.followed
  stats.value = statRes.data
}

const doFollow = async () => {
  try {
    const res = await api.post(`/user/${route.params.id}/follow`)
    ElMessage.success(res.data)
    followed.value = !followed.value
  } catch (e) {}
}

const saveProfile = async () => {
  await api.put('/user/profile', editForm.value)
  ElMessage.success('已保存')
  showEdit.value = false
  localStorage.setItem('nickname', editForm.value.nickname)
  loadData()
}

onMounted(async () => {
  editForm.value.nickname = nickname.value
  editForm.value.bio = bio.value
  await loadData()
})
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 0 20px; }
.profile-header { display: flex; justify-content: space-between; align-items: flex-start; background: #fff; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.bio { color: #888; }
.stats { display: flex; gap: 15px; color: #666; font-size: 14px; margin-top: 10px; }
</style>
