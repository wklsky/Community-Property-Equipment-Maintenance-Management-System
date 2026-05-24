package com.property.system.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serializable;

@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = DEFAULT_PAGE_NUM;

    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = MAX_PAGE_SIZE, message = "每页大小最大为100")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    private String orderBy;

    private String orderDirection = "desc";

    public <T> Page<T> toPage() {
        return new Page<>(
                pageNum != null ? pageNum : DEFAULT_PAGE_NUM,
                pageSize != null ? Math.min(pageSize, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE
        );
    }

    public <T> Page<T> toPage(Class<T> clazz) {
        return toPage();
    }

    public int getOffset() {
        return (getPageNum() - 1) * getPageSize();
    }

    public int getLimit() {
        return Math.min(pageSize != null ? pageSize : DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    public boolean isAsc() {
        return "asc".equalsIgnoreCase(orderDirection);
    }
}
