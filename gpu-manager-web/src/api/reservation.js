import request from '../utils/request'

export function createReservation(data) {
  return request.post('/reservations', data)
}

export function getMyReservations() {
  return request.get('/reservations/my')
}

export function cancelReservation(id) {
  return request.put(`/reservations/${id}/cancel`)
}

export function getAdminReservations(params) {
  return request.get('/reservations', {
    params
  })
}