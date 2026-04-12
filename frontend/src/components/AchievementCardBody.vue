<script setup>
/**
 * AchievementCardBody - 成就卡片内容区组件
 *
 * 根据成就类别渲染对应的卡片内容
 * 包含所有 format* 格式化函数
 *
 * Props:
 *   item - 成就对象，包含 category, title, fields 等
 */

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
})

// --- Contest ---
function formatContestDate(fields = {}) {
  return fields.awardDate || '-'
}

function formatContestAwardPill(fields = {}) {
  const parts = [fields.awardCategory, fields.awardLevel].filter(Boolean)
  return parts.join(' ')
}

function formatContestCategoryLine(fields = {}) {
  const parts = [fields.contestCategory, fields.contestType].filter(Boolean)
  const text = parts.length ? parts.join(' · ') : '-'
  return `类别：${text}`
}

function formatContestMembers(fields = {}) {
  const peopleParts = [fields.studentName, fields.teamMembers].filter(Boolean)
  const people = peopleParts.length ? peopleParts.join('、') : '-'
  const teachers = fields.instructors ? ` | 指导老师：${fields.instructors}` : ''
  return `成员：${people}${teachers}`
}

function formatContestAwardLine(fields = {}) {
  const count = fields.awardCount || '-'
  const remark = fields.remark ? ` · ${fields.remark}` : ''
  return `获奖人数：${count}${remark}`
}

function formatContestOrganizer(fields = {}) {
  return `主办单位：${fields.organizer || '-'}`
}

// --- Paper ---
function formatPaperJournal(fields = {}) {
  return fields.journalName || '-'
}

function formatPaperAuthors(fields = {}) {
  const name = fields.studentName || '-'
  const order = fields.authorOrder ? `（${fields.authorOrder}）` : ''
  return `${name}${order}`
}

function formatPaperDate(fields = {}) {
  return fields.publishDate || '-'
}

// --- Journal ---
function formatJournalPublication(fields = {}) {
  return fields.publicationName || '-'
}

function formatJournalAuthor(fields = {}) {
  return fields.studentName || '-'
}

function formatJournalDate(fields = {}) {
  return fields.publishDate || '-'
}

// --- Patent ---
function formatPatentGrantNo(fields = {}) {
  return fields.grantNo || '-'
}

function formatPatentInventor(fields = {}) {
  const name = fields.studentName || '-'
  const first = fields.firstInventor ? `（${fields.firstInventor}）` : ''
  return `${name}${first}`
}

function formatPatentDate(fields = {}) {
  return fields.grantDate || '-'
}

function formatPatentTag(fields = {}) {
  return fields.patentType || '专利'
}

// --- Certificate ---
function formatCertificateType(fields = {}) {
  return fields.certificateType || '-'
}

function formatCertificateOwner(fields = {}) {
  return fields.studentName || '-'
}

function formatCertificateDate(fields = {}) {
  return fields.obtainDate || '-'
}

// --- Research ---
function formatResearchLeader(fields = {}) {
  return `项目负责人：${fields.projectLeader || '-'}`
}

function formatResearchTeacher(fields = {}) {
  return `教师工号：${fields.teacherNo || '-'}`
}

// --- Works ---
function formatWorksCategory(fields = {}) {
  return fields.workCategory || '-'
}

function formatWorksOccasion(fields = {}) {
  return fields.publishOccasion || '-'
}

function formatWorksDate(fields = {}) {
  return fields.publishDate || '-'
}

function formatWorksTag(fields = {}) {
  return fields.workCategory || '作品'
}
</script>

<template>
  <div v-if="item.category === 'contest'" class="achievement-card-body">
    <div class="achievement-card-title-row">
      <div class="achievement-card-title">{{ item.title || '-' }}</div>
      <span v-if="formatContestAwardPill(item.fields)" class="achievement-award-pill">
        {{ formatContestAwardPill(item.fields) }}
      </span>
    </div>
    <div class="achievement-card-date-italic">
      {{ formatContestDate(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatContestCategoryLine(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatContestMembers(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatContestAwardLine(item.fields) }}
    </div>
    <div class="achievement-card-organizer">
      {{ formatContestOrganizer(item.fields) }}
    </div>
  </div>

  <div v-else-if="item.category === 'paper'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-text">
      {{ formatPaperJournal(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatPaperAuthors(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatPaperDate(item.fields) }}
    </div>
    <div class="achievement-paper-tag" aria-hidden="true">学术论文</div>
  </div>

  <div v-else-if="item.category === 'journal'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-text">
      {{ formatJournalPublication(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatJournalAuthor(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatJournalDate(item.fields) }}
    </div>
    <div class="achievement-journal-tag" aria-hidden="true">
      期刊作品
    </div>
  </div>

  <div v-else-if="item.category === 'patent'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-text">
      {{ formatPatentGrantNo(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatPatentInventor(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatPatentDate(item.fields) }}
    </div>
    <div class="achievement-patent-tag" aria-hidden="true">
      {{ formatPatentTag(item.fields) }}
    </div>
  </div>

  <div v-else-if="item.category === 'certificate'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-text">
      {{ formatCertificateType(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatCertificateOwner(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatCertificateDate(item.fields) }}
    </div>
    <div class="achievement-certificate-tag" aria-hidden="true">
      职业资格证书
    </div>
  </div>

  <div v-else-if="item.category === 'research'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-text">
      {{ formatResearchLeader(item.fields) }}
    </div>
    <div class="achievement-card-text">
      {{ formatResearchTeacher(item.fields) }}
    </div>
    <div class="achievement-research-tag" aria-hidden="true">
      科研项目
    </div>
  </div>

  <div v-else-if="item.category === 'works'" class="achievement-card-body">
    <div class="achievement-card-title achievement-paper-title">
      {{ item.title || '-' }}
    </div>
    <div class="achievement-card-meta">
      <span class="achievement-card-meta-item">{{ formatWorksCategory(item.fields) }}</span>
      <span class="achievement-card-meta-dot">·</span>
      <span class="achievement-card-meta-item">{{ formatWorksDate(item.fields) }}</span>
    </div>
    <div class="achievement-card-text" v-if="item.fields.publishOccasion">
      {{ formatWorksOccasion(item.fields) }}
    </div>
    <div class="achievement-card-text" v-if="item.fields.studentName">
      {{ item.fields.studentName }}
    </div>
    <div class="achievement-works-tag" aria-hidden="true">
      {{ formatWorksTag(item.fields) }}
    </div>
  </div>

  <div v-else class="achievement-card-body">
    <div class="achievement-card-title">{{ item.title || '-' }}</div>
    <div v-if="item.dateLabel" class="achievement-card-dates">
      <span>{{ item.dateLabel }}：{{ item.dateValue || '-' }}</span>
    </div>
    <div
      v-for="line in item.previewFields"
      :key="line"
      class="achievement-card-text"
    >
      {{ line }}
    </div>
  </div>
</template>
