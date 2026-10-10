<template>
  <div class="container">
    <div class="banner" :style="{ backgroundImage: banner ? `url(${banner})` : 'linear-gradient(135deg,#1e80ff,#7c3aed)' }">
      <div class="banner-overlay">
        <el-avatar :size="80" :src="avatar || undefined">{{ nickname[0] }}</el-avatar>
        <div class="banner-info">
          <h2>{{ nickname }}</h2>
          <p class="bio">{{ bio || '这个人很懒，什么都没写' }}</p>
        </div>
      </div>
    </div>
    <div class="stats-bar">
      <span>{{ stats.articleCount || 0 }} 文章</span>
      <span>{{ stats.likeReceived || 0 }} 获赞</span>
      <span class="clickable" @click="showFollowers = true">{{ followerCount }} 粉丝</span>
      <span class="clickable" @click="showFollowing = true">{{ followCount }} 关注</span>
    </div>
    <div class="actions-row">
      <el-button v-if="isSelf" @click="showEdit = true">编辑资料</el-button>
      <el-button v-if="isSelf" @click="showPwd = true">修改密码</el-button>
      <el-button v-if="isSelf" @click="showSettings = true">账号设置</el-button>
      <el-button v-if="!isSelf" :type="followed ? 'default' : 'primary'" @click="doFollow">
        {{ followed ? '已关注' : '关注' }}
      </el-button>
    </div>
    <el-empty v-if="list.length === 0" description="还没有发布文章" />
    <ArticleCard v-for="a in list" :key="a.id" :article="a" />

    <!-- 编辑资料 -->
    <el-dialog v-model="showEdit" title="编辑资料" width="460px" append-to-body>
      <div style="display:flex;align-items:center;gap:15px;margin-bottom:15px">
        <el-avatar :size="60" :src="editForm.avatar || undefined">{{ nickname[0] }}</el-avatar>
        <el-upload :show-file-list="false" action="/api/upload" :headers="uploadHeaders"
          :on-success="onAvatarUpload" :before-upload="beforeUpload">
          <el-button size="small" type="primary">上传头像</el-button>
        </el-upload>
      </div>
      <el-input v-model="editForm.nickname" placeholder="昵称" style="margin-bottom:10px" />
      <el-input v-model="editForm.bio" type="textarea" placeholder="简介" style="margin-bottom:10px" />
      <el-input v-model="editForm.banner" placeholder="背景图URL" style="margin-bottom:10px" />
      <el-upload :show-file-list="false" action="/api/upload" :headers="uploadHeaders"
        :on-success="onBannerUpload" :before-upload="beforeUpload">
        <el-button size="small">上传背景图</el-button>
      </el-upload>
      <el-button type="primary" @click="saveProfile" style="margin-top:10px">保存</el-button>
    </el-dialog>

    <!-- 修改密码 -->
    <el-dialog v-model="showPwd" title="修改密码" width="400px">
      <el-input v-model="pwdForm.oldPassword" type="password" placeholder="原密码" style="margin-bottom:10px" />
      <el-input v-model="pwdForm.newPassword" type="password" placeholder="新密码" style="margin-bottom:10px" />
      <el-button type="primary" @click="changePwd">确认修改</el-button>
    </el-dialog>

    <!-- 账号设置 -->
    <el-dialog v-model="showSettings" title="账号设置" width="420px">
      <el-input v-model="phone" placeholder="绑定手机号" style="margin-bottom:10px" />
      <el-button @click="bindPhone" style="margin-bottom:20px">保存手机号</el-button>
      <el-divider />
      <div style="color:#e6a23c;font-size:13px;margin-bottom:10px">危险操作</div>
      <el-button type="danger" plain @click="deactivate">注销账号</el-button>
    </el-dialog>

    <!-- 粉丝列表 -->
    <el-dialog v-model="showFollowers" title="粉丝" width="400px">
      <div v-for="f in followers" :key="f.userId" class="follow-item" @click="$router.push('/user/' + f.userId); showFollowers=false">
        <el-avatar :size="32" :src="f.avatar || undefined">{{ f.nickname[0] }}</el-avatar>
        <span>{{ f.nickname }}</span>
      </div>
    </el-dialog>

    <!-- 关注列表 -->
    <el-dialog v-model="showFollowing" title="我关注的人" width="400px">
      <div v-for="f in following" :key="f.userId" class="follow-item" @click="$router.push('/user/' + f.userId); showFollowing=false">
        <el-avatar :size="32" :src="f.avatar || undefined">{{ f.nickname[0] }}</el-avatar>
        <span>{{ f.nickname }}</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import ArticleCard from '../components/ArticleCard.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const list = ref([])
const nickname = ref('用户')
const bio = ref('')
const avatar = ref('')
const banner = ref('')
const stats = ref({})
const followerCount = ref(0)
const followCount = ref(0)
const followed = ref(false)
const showEdit = ref(false)
const showPwd = ref(false)
const showSettings = ref(false)
const showFollowers = ref(false)
const showFollowing = ref(false)
const followers = ref([])
const following = ref([])
const phone = ref('')
const editForm = ref({ nickname: '', bio: '', avatar: '', banner: '' })
const pwdForm = ref({ oldPassword: '', newPassword: '' })
const myUserId = ref(localStorage.getItem('userId'))
const authHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }
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
  avatar.value = userRes.data.avatar
  banner.value = userRes.data.banner
  followerCount.value = userRes.data.followerCount
  followCount.value = userRes.data.followCount
  followed.value = userRes.data.followed
  stats.value = statRes.data
}

const doFollow = async () => {
  const res = await api.post(`/user/${route.params.id}/follow`)
  ElMessage.success(res.data)
  followed.value = !followed.value
}

const saveProfile = async () => {
  await api.put('/user/profile', editForm.value)
  ElMessage.success('已保存')
  showEdit.value = false
  localStorage.setItem('nickname', editForm.value.nickname)
  loadData()
}

const changePwd = async () => {
  try {
    await api.put('/user/password', pwdForm.value)
    ElMessage.success('密码已修改')
    showPwd.value = false
  } catch (e) { ElMessage.error('原密码错误') }
}

const beforeUpload = (file) => {
  const ok = file.type.startsWith('image/')
  if (!ok) ElMessage.warning('只能上传图片')
  return ok
}
const onAvatarUpload = (res) => {
  if (res.code === 200) {
    editForm.value.avatar = res.data.url
    ElMessage.success('头像上传成功，点保存生效')
  } else ElMessage.error('上传失败')
}
const onBannerUpload = (res) => {
  if (res.code === 200) {
    editForm.value.banner = res.data.url
    ElMessage.success('背景图上传成功，点保存生效')
  } else ElMessage.error('上传失败')
}

const bindPhone = async () => {
  await api.put('/user/phone', { phone: phone.value })
  ElMessage.success('手机号已绑定')
}

const deactivate = async () => {
  try {
    await ElMessageBox.confirm('注销后账号无法恢复，确认？', '警告', { type: 'warning' })
    await api.delete('/user/deactivate')
    localStorage.clear()
    window.location.href = '/'
  } catch (e) {}
}

const loadFollowers = async () => {
  const res = await api.get(`/user/${route.params.id}/followers`)
  followers.value = res.data || []
}
const loadFollowing = async () => {
  const res = await api.get(`/user/${route.params.id}/following`)
  following.value = res.data || []
}

onMounted(async () => {
  await loadData()
  editForm.value.nickname = nickname.value
  editForm.value.bio = bio.value
  editForm.value.avatar = avatar.value
  editForm.value.banner = banner.value
})
watch(showFollowers, v => { if (v) loadFollowers() })
watch(showFollowing, v => { if (v) loadFollowing() })
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 0 20px; }
.banner { height: 160px; border-radius: 8px; background-size: cover; background-position: center; margin-bottom: 16px; }
.banner-overlay { display: flex; align-items: flex-end; gap: 16px; padding: 20px; background: linear-gradient(to top, rgba(0,0,0,0.4), transparent); height: 100%; box-sizing: border-box; }
.banner-info h2 { color: #fff; margin: 0; }
.banner-info .bio { color: rgba(255,255,255,0.8); margin: 4px 0 0; font-size: 13px; }
.stats-bar { display: flex; gap: 20px; padding: 12px 0; border-bottom: 1px solid #eee; }
.stats-bar span { color: #666; font-size: 14px; }
.clickable { cursor: pointer; }
.clickable:hover { color: #1e80ff; }
.actions-row { margin: 12px 0; }
.follow-item { display: flex; align-items: center; gap: 10px; padding: 10px; cursor: pointer; }
.follow-item:hover { background: #f5f5f5; }
</style>
