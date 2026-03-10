import request from './request'

export function getPosts(type) {
  return request.get('/api/posts', { params: { type } })
}

export function createPost(data) {
  return request.post('/api/posts', data)
}

export function deletePost(id) {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.delete(`/api/posts/${id}`, { headers })
}

export function uploadMedia(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/api/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
