/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/types/device.ts
 * @Description: 设备与设备分类的类型定义，字段对齐后端 Device / DeviceCategory 实体
 */

import type { PageQuery } from './common'

export interface Device {
  id: number
  tenantId: number
  buildingId?: number | null
  categoryId?: number | null
  name: string
  model?: string | null
  installDate?: string | null
  location?: string | null
  status: number
  qrCodeUrl?: string | null
  createTime?: string | null
}

export interface DeviceCategory {
  id: number
  tenantId: number
  name: string
}

export interface DeviceQuery extends PageQuery {
  categoryId?: number | null
  buildingId?: number | null
  status?: number | null
}
