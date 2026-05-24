import { get } from '../utils/request'

export const getCategories = () => get('/device-categories')

export const getBuildings = (communityId) => get('/buildings', communityId ? { communityId } : {})

export const getCommunities = () => get('/communities')

export const getDicts = (dictType) => get('/dicts', { dictType })
