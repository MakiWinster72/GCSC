<template>
  <div class="dashboard-layout">
    <transition name="publisher-backdrop">
      <div
        v-if="sidebarOpen"
        class="mobile-sidebar-backdrop"
        @click="closeSidebar"
      ></div>
    </transition>
    <aside class="dashboard-left" :class="{ open: sidebarOpen }">
      <section class="profile-card">
        <div class="profile-row profile-main">
          <div class="profile-avatar">
            <img
              v-if="profile.avatarUrl"
              :src="resolveMediaUrl(profile.avatarUrl)"
              alt="头像"
            />
            <span v-else>{{ avatarText }}</span>
          </div>
          <div class="profile-name-wrap">
            <p class="profile-name">
              {{ profile.displayName || profile.username || "同学" }}
            </p>
            <p class="profile-role">{{ roleLabel }}</p>
          </div>
          <button
            class="profile-settings"
            type="button"
            aria-label="设置"
            @click="goToSettings"
          >
            <img src="/assets/icons/settings.svg" alt="设置" />
          </button>
        </div>
        <div class="profile-row">学号：{{ profile.studentNo || "未填写" }}</div>
        <div class="profile-row">班级：{{ profile.className || "未填写" }}</div>
        <div class="profile-row">学院：{{ profile.college || "未填写" }}</div>
      </section>

      <section class="menu-card">
        <template v-for="item in menuItems" :key="item.key">
          <div
            v-if="item.key === 'achievements'"
            class="menu-drawer"
            :class="{ open: achievementsOpen }"
          >
            <button
              class="menu-item menu-drawer-trigger"
              :class="{
                active: activeMenu === item.key,
                disabled: !isMenuEnabled(item.key),
              }"
              type="button"
              :disabled="!isMenuEnabled(item.key)"
              @click="toggleAchievements"
            >
              <span>{{ item.label }}</span>
              <span class="menu-drawer-caret" aria-hidden="true"></span>
            </button>
            <div v-show="achievementsOpen" class="menu-drawer-panel">
              <button
                v-for="entry in achievementEntries"
                :key="entry.key"
                class="menu-drawer-item"
                :class="{ active: activeCategory === entry.key }"
                type="button"
                @click="handleAchievementEntry(entry.key)"
              >
                {{ entry.label }}
              </button>
            </div>
          </div>
          <button
            v-else
            class="menu-item"
            :class="{
              active: activeMenu === item.key,
              disabled: !isMenuEnabled(item.key),
            }"
            type="button"
            :disabled="!isMenuEnabled(item.key)"
            @click="handleMenuClick(item.key)"
          >
            {{ item.label }}
          </button>
        </template>
      </section>
    </aside>

    <main class="dashboard-right">
      <header class="feed-header">
        <h1 class="feed-title">个人成就</h1>
        <div class="achievement-category-pill">
          {{ activeCategoryLabel }}
        </div>
      </header>

      <section v-if="activeCategory === 'all'" class="info-card achievement-intro-card">
        <div class="info-section-title">全部</div>
        <p class="achievement-intro-text">这里存放了我所有的成就！</p>
      </section>

      <div v-if="!filteredAchievements.length" class="empty-tip">
        {{ emptyMessage }}
      </div>
      <div v-if="errorMessage" class="form-tip">{{ errorMessage }}</div>

      <section class="achievement-list">
        <article
          v-for="item in filteredAchievements"
          :key="item.id"
          class="achievement-card"
          @click="openDetail(item)"
        >
          <div class="achievement-card-image">
            <img v-if="item.image" :src="item.image" alt="成就图片" />
            <div v-else class="achievement-card-placeholder">未上传图片</div>
          </div>
          <div class="achievement-card-body">
            <div class="achievement-card-title">{{ item.name }}</div>
            <div class="achievement-card-dates">
              <span>起止：{{ item.startDate || "-" }} {{ item.endDate || "-" }}</span>
              <span class="date-spacer"></span>
              <span>获得日期：{{ item.awardDate || "-" }}</span>
            </div>
            <div class="achievement-card-text">
              {{ item.description || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ item.thoughts || "-" }}
            </div>
          </div>
        </article>
      </section>

      <button
        class="achievement-add"
        type="button"
        :aria-label="addButtonLabel"
        @click="openEditorForCategory"
      >
        <span aria-hidden="true">+</span>
      </button>
      <div class="mobile-capsule">
        <div class="capsule-left">
          <div
            class="capsule-action"
            role="button"
            tabindex="0"
            @click="openSidebar"
          >
            <span class="capsule-icon" aria-hidden="true">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </span>
          </div>
        </div>
        <div class="capsule-right">
          <div
            class="capsule-action capsule-primary"
            role="button"
            tabindex="0"
            :aria-label="addButtonLabel"
            @click="openEditorForCategory"
          >
            +
          </div>
        </div>
      </div>

      <transition name="publisher-backdrop">
        <div
          v-if="viewOpen"
          class="achievement-backdrop"
          @click="closeView"
        ></div>
      </transition>
      <section
        class="achievement-view"
        :class="{ open: viewOpen, closing: viewClosing, 'exit-up': viewExitUp }"
        :aria-hidden="!viewOpen"
      >
        <header class="publisher-header">
          <div class="publisher-title">成就查看</div>
          <button class="publisher-close" type="button" @click="closeView">
            关闭
          </button>
        </header>
        <div v-if="viewLoading" class="empty-tip">加载中...</div>
        <div v-else-if="viewItem" class="achievement-view-body">
          <div class="achievement-detail-image">
            <img v-if="viewItem.image" :src="viewItem.image" alt="成就图片" />
            <div v-else class="achievement-card-placeholder">未上传图片</div>
          </div>
          <div class="achievement-detail-body">
            <div class="achievement-card-title">{{ viewItem.name }}</div>
            <div class="achievement-card-dates">
              <span>起止：{{ viewItem.startDate || "-" }} {{ viewItem.endDate || "-" }}</span>
              <span class="date-spacer"></span>
              <span>获得日期：{{ viewItem.awardDate || "-" }}</span>
            </div>
            <div class="achievement-card-text">{{ viewItem.description || "-" }}</div>
            <div class="achievement-card-text">{{ viewItem.thoughts || "-" }}</div>
            <div class="achievement-card-actions">
              <button class="post-action" type="button" @click="editFromView">
                编辑
              </button>
              <button class="post-action danger" type="button" @click="openDelete">
                删除
              </button>
            </div>
          </div>
        </div>
      </section>

      <transition name="publisher-backdrop">
        <div
          v-if="editorOpen"
          class="achievement-backdrop"
          @click="closeEditor"
        ></div>
      </transition>
      <section
        class="achievement-sheet"
        :class="{ open: editorOpen }"
        :aria-hidden="!editorOpen"
      >
        <header class="publisher-header">
          <div class="publisher-title">{{ editId ? "编辑成就" : "新增成就" }}</div>
          <button class="publisher-close" type="button" @click="closeEditor">
            关闭
          </button>
        </header>
        <div class="achievement-grid">
          <button class="achievement-image" type="button" @click="triggerImage">
            <img v-if="imagePreview" :src="imagePreview" alt="成就图片" />
            <span v-else>选择图片</span>
          </button>

          <div class="achievement-fields">
            <div v-if="showCategorySelect" class="field-row">
              <label class="field-label">成就分类</label>
              <select v-model="form.category" class="form-input">
                <option disabled value="">请选择分类</option>
                <option
                  v-for="entry in categoryOptions"
                  :key="entry.key"
                  :value="entry.key"
                >
                  {{ entry.label }}
                </option>
              </select>
            </div>
            <div v-else class="field-row">
              <label class="field-label">成就分类</label>
              <div class="achievement-category-tag">
                {{ activeCategoryLabel }}
              </div>
            </div>
            <div class="field-row">
              <label class="field-label">成就名字</label>
              <input v-model="form.name" class="form-input" type="text" placeholder="请输入成就名称" />
            </div>

            <div class="field-row">
              <label class="field-label">获得日期</label>
              <div class="date-range-inline">
                <label class="date-input inline">
                  <span class="date-label">开始</span>
                  <input v-model="form.startDate" class="date-input-field" type="date" />
                </label>
                <span class="date-spacer"></span>
                <label class="date-input inline">
                  <span class="date-label">结束</span>
                  <input v-model="form.endDate" class="date-input-field" type="date" />
                </label>
                <span class="date-spacer"></span>
                <label class="date-input inline">
                  <span class="date-label">获得日期</span>
                  <input v-model="form.awardDate" class="date-input-field" type="date" />
                </label>
              </div>
            </div>

            <div class="field-row">
              <label class="field-label">成就描述</label>
              <textarea
                v-model="form.description"
                class="publisher-input"
                rows="2"
                placeholder="描述该成就的背景或亮点"
              ></textarea>
            </div>

            <div class="field-row">
              <label class="field-label">个人感想</label>
              <textarea
                v-model="form.thoughts"
                class="publisher-input"
                rows="2"
                placeholder="写下你的感受或收获"
              ></textarea>
            </div>

            <div class="achievement-actions">
              <button class="ghost-button" type="button" @click="closeEditor">
                取消
              </button>
              <button class="action-button" type="button" @click="saveAchievement">
                {{ editId ? "保存修改" : "保存成就" }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <transition name="dialog-fade">
        <div
          v-if="deleteDialogOpen"
          class="dialog-backdrop"
          @click="closeDelete"
        ></div>
      </transition>
      <transition name="dialog-pop">
        <section v-if="deleteDialogOpen" class="dialog-card" @click.stop>
          <header class="dialog-header">确认删除</header>
          <div class="dialog-body">删除后无法恢复，确定要删除这条动态吗？</div>
          <div class="dialog-actions">
            <button class="ghost-button" type="button" @click="closeDelete">
              取消
            </button>
            <button class="action-button" type="button" :disabled="deleteBusy" @click="confirmDelete">
              确定删除
            </button>
          </div>
        </section>
      </transition>

      <input
        ref="imageInput"
        type="file"
        accept="image/*"
        hidden
        @change="onImageChange"
      />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";
import {
  createAchievement,
  deleteAchievement,
  getAchievements,
  updateAchievement,
} from "../api/achievements";
import { uploadMedia } from "../api/upload";
import { API_BASE } from "../api/request";

const router = useRouter();
const route = useRoute();
const profile = reactive(loadUser());
const activeMenu = ref("achievements");
const editorOpen = ref(false);
const imageInput = ref(null);
const imagePreview = ref("");
const errorMessage = ref("");
const viewOpen = ref(false);
const viewLoading = ref(false);
const viewItem = ref(null);
const viewClosing = ref(false);
const editId = ref(null);
const deleteDialogOpen = ref(false);
const deleteBusy = ref(false);
const sidebarOpen = ref(false);
const achievementsOpen = ref(true);
const activeCategory = ref("all");

const achievements = ref([]);

const achievementEntries = [
  { key: "all", label: "全部" },
  { key: "contest", label: "学科竞赛、文体艺术" },
  { key: "paper", label: "发表学术论文" },
  { key: "journal", label: "发表期刊作品" },
  { key: "patent", label: "专利(著作权)授权数(项)" },
  { key: "certificate", label: "职业资格证书" },
  { key: "research", label: "学生参与教师科研项目情况" },
  { key: "works", label: "创作、表演的代表性作品" },
];

const form = reactive({
  name: "",
  startDate: "",
  endDate: "",
  awardDate: "",
  description: "",
  thoughts: "",
  imageUrl: "",
  category: "",
});

const menuItems = computed(() => filterMenuItemsByRole(profile.role));
const categoryOptions = computed(() =>
  achievementEntries.filter((entry) => entry.key !== "all"),
);

const roleLabelMap = {
  STUDENT: "学生",
  TEACHER: "教师",
  ADMIN: "管理员",
};

const roleLabel = computed(() => roleLabelMap[profile.role] || "学生");
const avatarText = computed(() => {
  const name = profile.displayName || profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});
const activeCategoryLabel = computed(() => {
  const match = achievementEntries.find((entry) => entry.key === activeCategory.value);
  return match ? match.label : "全部";
});
const showCategorySelect = computed(() => activeCategory.value === "all");
const filteredAchievements = computed(() => {
  if (activeCategory.value === "all") {
    return achievements.value;
  }
  return achievements.value.filter((item) => item.category === activeCategory.value);
});
const emptyMessage = computed(() => {
  if (activeCategory.value === "all") {
    return "还没有成就，点击右下角添加。";
  }
  return "该分类暂无成就，点击右下角添加。";
});
const addButtonLabel = computed(() => {
  if (activeCategory.value === "all") {
    return "添加成就";
  }
  return `添加${activeCategoryLabel.value}`;
});

function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return {
      username: raw.username || "",
      displayName: raw.displayName || "",
      avatarUrl: raw.avatarUrl || "",
      role: raw.role || "STUDENT",
      studentNo: raw.studentNo || "",
      className: raw.className || "",
      college: raw.college || "",
    };
  } catch {
    return {
      username: "",
      displayName: "",
      avatarUrl: "",
      role: "STUDENT",
      studentNo: "",
      className: "",
      college: "",
    };
  }
}

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  sidebarOpen.value = false;
  if (key === "achievements") {
    handleAchievementEntry("all");
    return;
  }
  if (key === "my-info") {
    router.push("/myinfos");
    return;
  }
  if (key === "student-info") {
    router.push("/student-info");
    return;
  }
  router.push("/myinfos");
}

function toggleAchievements() {
  if (!isMenuEnabled("achievements")) {
    return;
  }
  achievementsOpen.value = !achievementsOpen.value;
  activeMenu.value = "achievements";
  if (achievementsOpen.value) {
    handleAchievementEntry("all");
  }
}

function handleAchievementEntry(key) {
  if (!isMenuEnabled("achievements")) {
    return;
  }
  const safeKey = achievementEntries.some((entry) => entry.key === key) ? key : "all";
  activeCategory.value = safeKey;
  achievementsOpen.value = true;
  activeMenu.value = "achievements";
  sidebarOpen.value = false;
  router.push({ path: "/achievements", query: { category: safeKey } });
}

function openSidebar() {
  sidebarOpen.value = true;
}

function closeSidebar() {
  sidebarOpen.value = false;
}

function openEditorForCategory() {
  editId.value = null;
  resetForm();
  form.category = activeCategory.value === "all" ? "" : activeCategory.value;
  editorOpen.value = true;
}

function closeEditor() {
  editorOpen.value = false;
  editId.value = null;
}

function goToSettings() {
  router.push("/settings");
}

function resetForm() {
  form.name = "";
  form.startDate = "";
  form.endDate = "";
  form.awardDate = "";
  form.description = "";
  form.thoughts = "";
  form.imageUrl = "";
  form.category = "";
  imagePreview.value = "";
}

async function saveAchievement() {
  const payload = {
    name: form.name.trim(),
    startDate: form.startDate || null,
    endDate: form.endDate || null,
    awardDate: form.awardDate || null,
    description: form.description.trim(),
    thoughts: form.thoughts.trim(),
    imageUrl: form.imageUrl || null,
    category: form.category || (activeCategory.value === "all" ? null : activeCategory.value),
  };
  try {
    if (editId.value) {
      const { data } = await updateAchievement(editId.value, payload);
      achievements.value = achievements.value.map((item) =>
        item.id === data.id ? normalizeAchievement(data) : item,
      );
      if (viewItem.value && viewItem.value.id === data.id) {
        viewItem.value = normalizeAchievement(data);
      }
    } else {
      const { data } = await createAchievement(payload);
      achievements.value = [normalizeAchievement(data), ...achievements.value];
    }
    resetForm();
    closeEditor();
    errorMessage.value = "";
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || "保存失败，请重新登录";
  }
}

function triggerImage() {
  imageInput.value && imageInput.value.click();
}

async function onImageChange(event) {
  const [file] = Array.from(event.target.files || []);
  event.target.value = "";
  if (!file) {
    return;
  }
  const { data } = await uploadMedia(file);
  form.imageUrl = data.url;
  imagePreview.value = resolveMediaUrl(data.url);
}

function resolveMediaUrl(url) {
  if (!url) {
    return "";
  }
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }
  return `${API_BASE}${url}`;
}

function normalizeAchievement(item) {
  return {
    id: item.id,
    name: item.name,
    startDate: item.startDate,
    endDate: item.endDate,
    awardDate: item.awardDate,
    description: item.description,
    thoughts: item.thoughts,
    image: resolveMediaUrl(item.imageUrl),
    category: item.category || "",
  };
}

function openDetail(item) {
  viewItem.value = item;
  viewOpen.value = true;
  viewClosing.value = false;
}

function closeView() {
  viewOpen.value = false;
  viewClosing.value = true;
  setTimeout(() => {
    viewItem.value = null;
    viewClosing.value = false;
  }, 260);
}

function editFromView() {
  if (!viewItem.value) {
    return;
  }
  const item = viewItem.value;
  editId.value = item.id;
  form.name = item.name || "";
  form.startDate = item.startDate || "";
  form.endDate = item.endDate || "";
  form.awardDate = item.awardDate || "";
  form.description = item.description || "";
  form.thoughts = item.thoughts || "";
  form.category = item.category || "";
  form.imageUrl = item.image ? item.image.replace(API_BASE, "") : "";
  imagePreview.value = item.image || "";
  viewOpen.value = false;
  viewClosing.value = true;
  editorOpen.value = true;
  setTimeout(() => {
    viewItem.value = null;
    viewClosing.value = false;
  }, 260);
}

function openDelete() {
  deleteDialogOpen.value = true;
}

function closeDelete() {
  if (deleteBusy.value) {
    return;
  }
  deleteDialogOpen.value = false;
}

async function confirmDelete() {
  if (!viewItem.value) {
    closeDelete();
    return;
  }
  deleteBusy.value = true;
  try {
    await deleteAchievement(viewItem.value.id);
    achievements.value = achievements.value.filter((item) => item.id !== viewItem.value.id);
    closeDelete();
    closeView();
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || "删除失败";
  } finally {
    deleteBusy.value = false;
  }
}

async function fetchAchievements() {
  try {
    const { data } = await getAchievements();
    achievements.value = Array.isArray(data) ? data.map(normalizeAchievement) : [];
    errorMessage.value = "";
  } catch (err) {
    achievements.value = [];
    errorMessage.value = err?.response?.data?.message || "无法获取成就列表";
  }
}

function syncCategoryFromRoute() {
  const rawCategory = route.query.category;
  const safeCategory =
    typeof rawCategory === "string" &&
    achievementEntries.some((entry) => entry.key === rawCategory)
      ? rawCategory
      : "all";
  activeCategory.value = safeCategory;
  achievementsOpen.value = true;
  if (rawCategory !== safeCategory) {
    router.replace({ path: "/achievements", query: { category: safeCategory } });
  }
}

onMounted(() => {
  fetchAchievements();
  syncCategoryFromRoute();
});

watch(
  () => route.query.category,
  () => {
    syncCategoryFromRoute();
  },
);
</script>
