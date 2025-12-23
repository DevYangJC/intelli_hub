package com.intellihub.page;


import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



/**
 * 分页请求参数
 */
@Data
public class PageWrap<M> implements Serializable {


    private M model;

    private int page;

    private int capacity;

    private List<SortData> sorts;

    /**
     * 处理异常排序对象
     * @author Cordr.Mr
     * @date 2022/02/15 20:09
     */
    public List<SortData> getSorts () {
        List<SortData> sorts = new ArrayList<>();
        if (this.sorts == null) {
            return sorts;
        }
        for (SortData sort: this.sorts) {
            if (sort.getProperty() == null || sort.getProperty().trim().length() == 0) {
                continue;
            }
            if (sort.getDirection() == null || sort.getDirection().trim().length() == 0 || (!sort.getDirection().trim().equalsIgnoreCase("asc") && !sort.getDirection().trim().equalsIgnoreCase("desc"))) {
                continue;
            }
            sorts.add(sort);
        }
        return sorts;
    }

    /**
     * 处理异常页码
     * @author Cordr.Mr
     * @date 2022/02/15 20:09
     */
    public int getPage () {
        return page <= 0 ? 1 : page;
    }

    /**
     * 处理异常页容量
     * @author Cordr.Mr
     * @date 2022/02/15 20:09
     */
    public int getCapacity () {
        return capacity <= 0 ? 10 : capacity;
    }

    /**
     * 获取排序字符串
     * @author Cordr.Mr
     * @date 2022/02/15 20:09
     */
    public String getOrderByClause (boolean isSplitOrderBy) {
        List<SortData> sorts = this.getSorts();
        StringBuilder stringBuilder = new StringBuilder();
        for (SortData sortData: sorts) {
            // 防注入
            if (!sortData.getProperty().matches("[a-zA-Z0-9_\\.]+")) {
                continue;
            }
            stringBuilder.append(sortData.getProperty().trim());
            stringBuilder.append(" ");
            stringBuilder.append(sortData.getDirection().trim());
            stringBuilder.append(",");
        }
        if (stringBuilder.length() == 0) {
            return null;
        }
        String orderByStr = stringBuilder.substring(0, stringBuilder.length() - 1);
        return Boolean.TRUE.equals(isSplitOrderBy)?"ORDER BY " + orderByStr:orderByStr;
    }

    /**
     * 排序对象
     * @author Cordr.Mr
     * @date 2022/02/15 20:09
     */
    @Data
    public static class SortData {

        private String property;

        private String direction;
    }
}
