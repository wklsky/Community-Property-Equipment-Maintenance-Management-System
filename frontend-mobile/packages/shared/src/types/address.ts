/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/types/address.ts
 * @Description: 报修地址的类型定义，含系统房产与自定义地址两类来源
 */

/**
 * /my-addresses 返回的聚合地址。
 * type 决定 id 属于哪张表：property 走 /my-properties/{id}/default，
 * custom 走 /user-addresses/{id}/default，混用会 404
 */
export interface MyAddress {
  id: number
  type: 'property' | 'custom'
  address: string
  communityName?: string | null
  buildingName?: string | null
  roomNo?: string | null
  isDefault: number
}

/** 新增/编辑自定义地址的入参，字段对齐后端 UserAddress 实体 */
export interface UserAddressPayload {
  address: string
  isDefault?: number
}

/** 自定义地址实体，新增接口会回传完整记录 */
export interface UserAddress {
  id: number
  tenantId: number
  userId: number
  address: string
  isDefault: number
  createTime?: string | null
}
