package com.property.system.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long pageNum;

    private Long pageSize;

    private Long total;

    private Long pages;

    private List<T> records;

    private Boolean hasPrevious;

    private Boolean hasNext;

    public PageResult() {
        this.records = Collections.emptyList();
    }

    public PageResult(Long pageNum, Long pageSize, Long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
        this.pages = (total + pageSize - 1) / pageSize;
        this.hasPrevious = pageNum > 1;
        this.hasNext = pageNum < this.pages;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(page.getRecords());
        result.setHasPrevious(page.getCurrent() > 1);
        result.setHasNext(page.getCurrent() < page.getPages());
        return result;
    }

    public static <T, R> PageResult<R> of(IPage<T> page, Function<T, R> converter) {
        PageResult<R> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList()));
        result.setHasPrevious(page.getCurrent() > 1);
        result.setHasNext(page.getCurrent() < page.getPages());
        return result;
    }

    public static <T> PageResult<T> empty() {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(1L);
        result.setPageSize(10L);
        result.setTotal(0L);
        result.setPages(0L);
        result.setRecords(Collections.emptyList());
        result.setHasPrevious(false);
        result.setHasNext(false);
        return result;
    }
}
