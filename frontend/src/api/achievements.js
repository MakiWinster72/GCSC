import request from './request'

export function getAchievements() {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.get('/api/achievements', { headers })
}

export function createAchievement(data) {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.post('/api/achievements', data, { headers })
}
