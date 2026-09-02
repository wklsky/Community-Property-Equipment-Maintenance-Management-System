/**
 * @Author: kian
 * @Date: 2026-09-02 15:20
 * @LastEditors: kian
 * @LastEditTime: 2026-09-02 15:20
 * @FilePath: frontend-mobile/packages/shared/src/types/property.ts
 * @Description: 社区、楼栋、房间、数据字典与维修工选项等基础数据类型
 */

export interface Community {
  id: number
  tenantId: number
  name: string
}

export interface Building {
  id: number
  tenantId: number
  communityId: number
  name: string
}

export interface Room {
  id: number
  tenantId: number
  buildingId: number
  roomNo: string
}

/** 后端 sys_dict 记录，dictKey 为存储值，dictValue 为展示文案 */
export interface DictItem {
  id: number
  tenantId: number
  dictType: string
  dictKey: string
  dictValue: string
}

/** /workers 返回的是 SysUser 实体列表，下拉选择只需要这三个字段 */
export interface WorkerOption {
  id: number
  username: string
  phone?: string
}
