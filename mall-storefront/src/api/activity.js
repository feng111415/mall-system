import http from './http'

const base = '/mall/member/product-activity'

export const getActivitySummary = () => http.get(`${base}/summary`)
export const getFavorites = () => http.get(`${base}/favorites`)
export const getFavoriteState = spuId => http.get(`${base}/favorites/${spuId}/state`)
export const addFavorite = spuId => http.post(`${base}/favorites/${spuId}`)
export const removeFavorite = spuId => http.delete(`${base}/favorites/${spuId}`)
export const getBrowseHistory = () => http.get(`${base}/history`)
export const recordBrowseHistory = spuId => http.post(`${base}/history/${spuId}`)
export const removeBrowseHistory = spuId => http.delete(`${base}/history/${spuId}`)
export const removeBrowseHistoryBatch = spuIds => http.post(`${base}/history/batch-delete`, { spuIds })
export const clearBrowseHistory = () => http.delete(`${base}/history`)
