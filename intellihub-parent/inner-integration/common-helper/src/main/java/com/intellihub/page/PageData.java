package com.intellihub.page;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页数据
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class PageData<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages;

    public PageData() {
    }

    public PageData(long current, long size) {
        this.current = current;
        this.size = size;
    }

    public PageData(List<T> records, long total, long size, long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }

    /**
     * 设置总数并计算总页数
     */
    public void setTotal(long total) {
        this.total = total;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }

    public static <T> PageData<T> of(List<T> records, long total, long size, long current) {
        return new PageData<>(records, total, size, current);
    }
}
