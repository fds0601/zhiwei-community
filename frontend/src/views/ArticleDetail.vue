<template>
  <div class="page" v-if="article">
    <div class="detail-layout">
      <div class="main-col">
        <h1 class="title">{{ article.title }}</h1>
        <div class="meta">
          <span class="author" @click="$router.push('/user/' + article.userId)">
            {{ article.nickname || ('用户' + article.userId) }}
          </span>
          <span>{{ article.createTime?.substring(0,10) }}</span>
          <span>{{ article.viewCount }} 阅读</span>
        </div>
        <div v-if="article.coverUrl" class="cover-wrap">
          <img :src="article.coverUrl" class="cover" />
        </div>
        <div class="content" v-html="renderedContent"></div>
        <div class="actions">
          <el-button :type="liked ? 'primary' : 'default'" @click="doLike" :icon="Star">{{ liked ? '已点赞' : '点赞' }}</el-button>
          <el-button :type="favorited ? 'primary' : 'default'" @click="doFavorite" :icon="Collection">{{ favorited ? '已收藏' : '收藏' }}</el-button>
          <el-button @click="doReport" :icon="Warning">举报</el-button>
          <el-button v-if="isAuthor" type="warning" @click="$router.push('/publish/' + article.id)">编辑文章</el-button>
          <el-button v-if="isAuthor" type="danger" @click="doDelete">删除文章</el-button>
        </div>
        <el-divider />
        <h3>评论 ({{ comments.length }})</h3>
        <div v-if="token" class="comment-input">
          <el-input v-model="commentText" type="textarea" :rows="2" :placeholder="replyTo ? '回复 ' + replyTo.nickname + '：' : '写下你的评论'" />
          <div style="margin-top:8px;display:flex;gap:8px">
            <el-button @click="commentText = ''">取消</el-button>
            <el-button type="primary" @click="doComment">提交</el-button>
          </div>
        </div>
        <div v-for="c in rootComments" :key="c.id" class="comment">
          <div class="comment-head">
            <b @click="$router.push('/user/' + c.userId)" class="author">{{ c.nickname || ('用户' + c.userId) }}</b>
            <span class="comment-time">{{ c.createTime?.substring(0,16).replace('T',' ') }}</span>
          </div>
          <div class="comment-body">{{ c.content }}</div>
          <div class="comment-actions">
            <el-button link size="small" @click="doLikeComment(c)">
              <el-icon style="vertical-align:-2px"><StarFilled v-if="c.liked" style="color:#1e80ff"/><Star v-else/></el-icon> {{ c.likeCount || 0 }}
            </el-button>
            <el-button v-if="token" link size="small" @click="replyTo = c">回复</el-button>
            <el-button v-if="token" link size="small" @click="doReportComment(c)">举报</el-button>
            <el-button v-if="isMyComment(c)" link type="danger" size="small" @click="delComment(c.id)">删除</el-button>
          </div>
          <template v-for="r in childrenOf(c.id)" :key="r.id">
            <div class="reply" style="margin-left:20px">
              <b @click="$router.push('/user/' + r.userId)" class="author">{{ r.nickname }}</b>
              <template v-if="r.replyToNickname"> 回复 <b>{{ r.replyToNickname }}</b></template>
              ：{{ r.content }}
              <span class="comment-actions">
                <el-button v-if="token" link size="small" @click="replyTo = c">回复</el-button>
                <el-button v-if="isMyComment(r)" link type="danger" size="small" @click="delComment(r.id)">删除</el-button>
              </span>
            </div>
          </template>
        </div>
      </div>
      <div class="side-col">
        <div class="author-card">
          <div class="author-head">
            <el-avatar :size="40" :src="article.avatarUrl" @click="$router.push('/user/' + article.userId)">{{ (article.nickname || 'U')[0] }}</el-avatar>
            <div class="author-info" @click="$router.push('/user/' + article.userId)">
              <div class="author-name">{{ article.nickname || ('用户' + article.userId) }}</div>
            </div>
            <el-button type="primary" size="small" @click="doFollow" style="margin-left:auto">
              {{ following ? '已关注' : '关注' }}
            </el-button>
          </div>
          <div class="author-stats">
            <div><b>{{ authorStats.articleCount }}</b><span>文章</span></div>
            <div><b>{{ authorStats.likeCount }}</b><span>获赞</span></div>
            <div><b>{{ authorStats.followerCount }}</b><span>粉丝</span></div>
          </div>
        </div>
        <div class="related-card" v-if="related.length" style="margin-top:16px">
          <h4>相关推荐</h4>
          <div v-for="a in related" :key="a.id" class="related-item" @click="$router.push('/article/' + a.id)">
            <div class="related-title">{{ a.title }}</div>
            <div class="related-meta">{{ a.nickname }} · {{ a.viewCount }}阅读 · {{ a.likeCount }}赞</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import api from '../api'
import { Star, StarFilled, Collection, Warning, Pointer } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const md = new MarkdownIt({ linkify: true, breaks: true })
const article = ref(null)
const comments = ref([])
const related = ref([])
const authorStats = ref({ articleCount: 0, likeCount: 0, followerCount: 0 })
const commentText = ref('')
const replyTo = ref(null)
const liked = ref(false)
const favorited = ref(false)
const following = ref(false)
const reactionCounts = ref({})
const reactionMine = ref([])
const token = ref(localStorage.getItem('token'))
const myUserId = ref(localStorage.getItem('userId'))
const tags = computed(() => article.value?.tags ? article.value.tags.split(',') : [])
const renderedContent = computed(() => DOMPurify.sanitize(md.render(article.value?.content || '')))
const isAuthor = computed(() => article.value && myUserId.value == article.value.userId)
const rootComments = computed(() => comments.value.filter(c => !c.parentId || c.parentId === 0))
const childrenOf = (pid) => comments.value.filter(c => c.parentId === pid)

const loadDetail = async () => {
  const res = await api.get('/article/' + route.params.id)
  article.value = res.data.article
  comments.value = res.data.comments
  liked.value = res.data.liked || false
  favorited.value = res.data.favorited || false
  following.value = res.data.following || false
  related.value = res.data.related || []
  authorStats.value = {
    articleCount: res.data.authorArticleCount || 0,
    likeCount: res.data.authorLikeCount || 0,
    followerCount: res.data.authorFollowerCount || 0
  }
}
const doLike = async () => {
  if (!token.value) { ElMessage.warning('请先登录'); setTimeout(() => router.push('/login'), 500); return }
  try {
    const res = await api.post(`/article/${route.params.id}/like`)
    ElMessage.success(res.data)
    liked.value = !liked.value
    article.value.likeCount += liked.value ? 1 : -1
  } catch (e) {}
}
const doFavorite = async () => {
  if (!token.value) { ElMessage.warning('请先登录'); setTimeout(() => router.push('/login'), 500); return }
  try {
    const res = await api.post(`/article/${route.params.id}/favorite`)
    ElMessage.success(res.data)
    favorited.value = !favorited.value
  } catch (e) {}
}
const doReport = async () => {
  if (!token.value) { ElMessage.warning('请先登录'); setTimeout(() => router.push('/login'), 500); return }
  try {
    const { value } = await ElMessageBox.prompt('请输入举报原因', '举报文章', { confirmButtonText: '确定', cancelButtonText: '取消' })
    await api.post(`/report`, { articleId: route.params.id, reason: value })
    ElMessage.success('举报成功，管理员将尽快处理')
  } catch (e) {}
}
const doReportComment = async (c) => {
  if (!token.value) { ElMessage.warning('请先登录'); setTimeout(() => router.push('/login'), 500); return }
  try {
    const { value } = await ElMessageBox.prompt('请输入举报原因', '举报评论', { confirmButtonText: '确定', cancelButtonText: '取消' })
    await api.post(`/report`, { commentId: c.id, reason: value })
    ElMessage.success('举报成功，管理员将尽快处理')
  } catch (e) {}
}
const doFollow = async () => {
  if (!token.value) { ElMessage.warning('请先登录'); setTimeout(() => router.push('/login'), 500); return }
  try {
    await api.post(`/user/${article.value.userId}/follow`)
    following.value = !following.value
    ElMessage.success(following.value ? '已关注' : '已取消关注')
  } catch (e) {}
}
const doDelete = async () => {
  try {
    await ElMessageBox.confirm('确认删除这篇文章？删除后不可恢复', '警告', { type: 'warning' })
    await api.delete('/article/' + route.params.id)
    ElMessage.success('已删除')
    router.push('/')
  } catch (e) {}
}
const doComment = async () => {
  try {
    await api.post(`/article/${route.params.id}/comment`, {
      content: commentText.value,
      parentId: replyTo.value ? replyTo.value.id : 0,
      replyToUserId: replyTo.value ? replyTo.value.userId : null
    })
    commentText.value = ''
    replyTo.value = null
    loadDetail()
  } catch (e) {}
}
const doLikeComment = async (c) => {
  if (!token.value) { ElMessage.warning('请先登录'); return }
  try {
    const res = await api.post(`/article/comment/${c.id}/like`)
    ElMessage.success(res.data)
    c.liked = !c.liked
    c.likeCount += c.liked ? 1 : -1
  } catch (e) {}
}
const isMyComment = (c) => myUserId.value == c.userId
const delComment = async (cid) => {
  try {
    await ElMessageBox.confirm('确认删除这条评论？', '提示', { type: 'warning' })
    await api.delete(`/article/comment/${cid}`)
    ElMessage.success('已删除')
    loadDetail()
  } catch (e) {}
}
onMounted(() => {
  loadDetail().then(() => {
    setTimeout(() => {
      document.querySelectorAll('.content img').forEach(img => {
        img.addEventListener('click', () => window.open(img.src, '_blank'))
      })
    }, 100)
  })
})
</script>

<style scoped>
.page { max-width: 1100px; margin: 20px auto; padding: 0 20px; }
.detail-layout { display: flex; gap: 24px; align-items: flex-start; }
.main-col { flex: 1; background: #fff; padding: 30px; border-radius: 8px; }
.side-col { width: 280px; flex-shrink: 0; }
.title { font-size: 24px; margin: 0 0 12px; }
.meta { color: #999; font-size: 13px; margin-bottom: 20px; display: flex; gap: 12px; align-items: center; }
.author { color: #1e80ff; cursor: pointer; font-weight: 500; }
.cover-wrap { width: 100%; border-radius: 8px; overflow: hidden; margin-bottom: 20px; }
.cover { width: 100%; display: block; }
.content { line-height: 1.8; font-size: 15px; }
.content :deep(img) { max-width: 100%; max-height: 500px; object-fit: contain; border-radius: 6px; display: block; margin: 10px auto; cursor: zoom-in; }
.actions { margin: 20px 0; }
.comment { padding: 12px 0; border-bottom: 1px solid #eee; }
.comment-head { display: flex; gap: 10px; align-items: center; }
.comment-time { color: #aaa; font-size: 12px; }
.comment-body { margin: 6px 0; }
.comment-actions { display: inline-flex; gap: 8px; }
.reply { margin: 8px 0 8px 20px; padding: 6px 10px; background: #f8f8f8; border-radius: 4px; font-size: 14px; }
.author-card { background: #fff; padding: 20px; border-radius: 8px; margin-bottom: 16px; }
.author-head { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.author-name { font-weight: 600; }
.author-stats { display: flex; justify-content: space-around; margin-bottom: 16px; text-align: center; }
.author-stats b { display: block; font-size: 18px; }
.author-stats span { font-size: 12px; color: #999; }
.related-card { background: #fff; padding: 20px; border-radius: 8px; }
.related-card h4 { margin: 0 0 12px; }
.related-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.related-item { padding: 15px; background: #fafafa; border-radius: 8px; cursor: pointer; }
.related-item:hover .related-title { color: #1e80ff; }
.related-title { font-size: 14px; margin-bottom: 4px; }
.related-meta { font-size: 12px; color: #999; }
</style>
