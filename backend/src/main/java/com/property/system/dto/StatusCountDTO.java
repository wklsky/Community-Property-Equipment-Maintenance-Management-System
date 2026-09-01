package com.property.system.dto;

import lombok.Data;

/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-01 10:50
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-01 10:50
 * @FilePath: backend/src/main/java/com/property/system/dto/StatusCountDTO.java
 * @Description: 按状态分组的计数结果，用于看板一次性聚合各状态数量
 */

/**
 * 看板原按状态逐个 selectCount，单次请求最多 17 条 SQL。
 * 改用 GROUP BY 后每个业务域固定 1 条，缺失的状态由调用方补 0。
 */
@Data
public class StatusCountDTO {

    private Integer status;

    private Long count;
}
