package com.example.srs.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long page;
    private long pageSize;
    private long totalPages;

    public static <T> PageResult<T> from(IPage<T> pageData) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(pageData.getRecords());
        result.setTotal(pageData.getTotal());
        result.setPage(pageData.getCurrent());
        result.setPageSize(pageData.getSize());
        result.setTotalPages(pageData.getPages());
        return result;
    }
}
