package com.intellihub.page;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求参数
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class PageWrap implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer page = 1;
    private Integer size = 10;

    public Integer getOffset() {
        return (page - 1) * size;
    }

    public static PageWrap of(Integer page, Integer size) {
        PageWrap wrap = new PageWrap();
        wrap.setPage(page != null ? page : 1);
        wrap.setSize(size != null ? size : 10);
        return wrap;
    }
}
