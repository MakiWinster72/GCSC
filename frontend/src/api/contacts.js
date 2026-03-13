import request from './request'

export function getContacts(params = {}) {
  return request.get('/api/contacts', { params })
}

export function createContact(data) {
  return request.post('/api/contacts', data)
}
