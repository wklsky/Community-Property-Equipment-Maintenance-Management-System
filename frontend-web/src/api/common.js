import request from '@/utils/request'

export function getCommunities() {
  return request.get('/communities')
}

export function getDeviceCategories() {
  return request.get('/device-categories')
}

export function getBuildings(communityId) {
  return request.get('/buildings', { params: { communityId } })
}

export function getRooms(buildingId) {
  return request.get('/rooms', { params: { buildingId } })
}

export function getWorkers() {
  return request.get('/workers')
}

export function getDicts(dictType) {
  return request.get('/dicts', { params: { dictType } })
}
