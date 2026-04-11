import request from './request'

export function getUserList({ page = 1, size = 20, search = '', role = '', className = '' } = {}) {
  const params = { page, size }
  if (search) params.search = search
  if (role) params.role = role
  if (className) params.className = className
  return request.get('/api/admin/users', { params })
}

export function updateUser(id, data) {
  return request.put(`/api/admin/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/api/admin/users/${id}`)
}
