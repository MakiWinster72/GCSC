<template>
  <main class="dashboard-right">
    <header class="feed-header">
      <h1 class="feed-title">班级审核</h1>
      <div class="feed-header-subtitle" v-if="profile.className">
        当前班级：{{ profile.className }}
      </div>
      <div class="feed-header-subtitle" v-else style="color: #e65c5c;">
        您还未填写班级，无法使用班级审核功能
      </div>
    </header>

    <section v-if="!profile.className" class="card empty-state-card">
      <div class="empty-tip">请先在「我的信息」中填写班级信息</div>
    </section>

    <section v-else class="card">
      <div class="info-section-title">待审核申请</div>

      <div v-if="loading" class="empty-tip">加载中...</div>
      <div v-else-if="!pendingRequests.length" class="empty-tip">暂无待审核的申请</div>
      <div v-else class="review-list">
        <div v-for="item in pendingRequests" :key="item.id + '-' + item.resourceType" class="review-item">
          <div class="review-item-header">
            <span class="review-badge" :class="item.resourceType === 'achievement' ? 'is-achievement' : 'is-profile'">
              {{ item.resourceType === 'achievement' ? '成就' : '信息' }}
            </span>
            <span class="review-requester">{{ item.requester?.displayName || item.requester?.username }}</span>
            <span class="review-time">{{ formatTime(item.updatedAt) }}</span>
          </div>
          <div class="review-item-title">{{ item.title }}</div>
          <div class="review-item-summary">{{ item.summary || '无' }}</div>
          <div class="review-item-actions">
            <button class="action-button" type="button" @click="approveRequest(item)">通过</button>
            <button class="ghost-button" type="button" @click="openReject(item)">驳回</button>
          </div>
        </div>
      </div>
    </section>

    <!-- Reject Dialog -->
    <div :class="['sheet-overlay', { open: rejectOpen }]" @click.self="closeReject">
      <div class="sheet-modal">
        <header class="sheet-modal-header">
          <div class="sheet-modal-title">驳回申请</div>
        </header>
        <div class="sheet-modal-body">
          <div class="reject-form">
            <label class="modal-label">驳回理由</label>
            <textarea v-model="rejectReason" class="info-input reject-textarea" placeholder="请输入驳回理由"></textarea>
          </div>
        </div>
        <div class="sheet-modal-actions">
          <button class="ghost-button" type="button" @click="closeReject">取消</button>
          <button class="action-button" type="button" @click="confirmReject" :disabled="!rejectReason.trim()">确认驳回</button>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useToast } from "../composables/useToast";
import { listAchievementReviewRequests, approveAchievementReviewRequest, rejectAchievementReviewRequest } from "../api/achievementReviewRequests";
import { listProfileReviewRequests, approveProfileReviewRequest, rejectProfileReviewRequest } from "../api/profileReviewRequests";
import { loadUser } from "../utils/userStorage";

const { success: toastSuccess, error: toastError } = useToast();
const profile = loadUser();
const loading = ref(true);
const achievementRequests = ref([]);
const profileRequests = ref([]);
const rejectOpen = ref(false);
const rejectReason = ref("");
const selectedItem = ref(null);

onMounted(async () => {
  await fetchRequests();
});

async function fetchRequests() {
  loading.value = true;
  try {
    const [achRes, profRes] = await Promise.all([
      listAchievementReviewRequests(),
      listProfileReviewRequests(),
    ]);
    achievementRequests.value = Array.isArray(achRes.data) ? achRes.data : [];
    profileRequests.value = Array.isArray(profRes.data) ? profRes.data : [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

const pendingRequests = computed(() => {
  const myClass = profile.className;
  if (!myClass) return [];

  const filterByClass = (item) => {
    const requester = item.requester;
    if (!requester) return false;
    const requesterClass = requester.className;
    if (!requesterClass) return false;
    return requesterClass.trim() === myClass.trim();
  };

  const achPending = achievementRequests.value
    .filter(r => r.status === 'pending' && filterByClass(r))
    .map(r => ({ ...r, resourceType: 'achievement' }));

  const profPending = profileRequests.value
    .filter(r => r.status === 'pending' && filterByClass(r))
    .map(r => ({ ...r, resourceType: 'profile' }));

  return [...achPending, ...profPending].sort((a, b) => {
    return new Date(b.updatedAt) - new Date(a.updatedAt);
  });
});

async function approveRequest(item) {
  try {
    if (item.resourceType === 'achievement') {
      await approveAchievementReviewRequest(item.id);
    } else {
      await approveProfileReviewRequest(item.id);
    }
    toastSuccess("审核已通过");
    await fetchRequests();
  } catch (e) {
    toastError("操作失败，请刷新重试");
  }
}

function openReject(item) {
  selectedItem.value = item;
  rejectReason.value = "";
  rejectOpen.value = true;
}

function closeReject() {
  rejectOpen.value = false;
  selectedItem.value = null;
}

async function confirmReject() {
  if (!rejectReason.value.trim()) return;
  try {
    if (selectedItem.value.resourceType === 'achievement') {
      await rejectAchievementReviewRequest(selectedItem.value.id, { reason: rejectReason.value.trim() });
    } else {
      await rejectProfileReviewRequest(selectedItem.value.id, { reason: rejectReason.value.trim() });
    }
    toastSuccess("已驳回");
    closeReject();
    await fetchRequests();
  } catch (e) {
    toastError("操作失败，请刷新重试");
  }
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}
</script>

<style scoped>
.feed-header-subtitle {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.empty-state-card {
  margin-top: 16px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.review-item {
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fafafa;
}

.review-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.review-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.review-badge.is-achievement {
  background: #e3f2fd;
  color: #1565c0;
}

.review-badge.is-profile {
  background: #e8f5e9;
  color: #2e7d32;
}

.review-requester {
  font-weight: 500;
  color: #333;
}

.review-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.review-item-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.review-item-summary {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
}

.review-item-actions {
  display: flex;
  gap: 8px;
}

.reject-textarea {
  width: 100%;
  min-height: 100px;
  resize: vertical;
  font-family: inherit;
}

.reject-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
