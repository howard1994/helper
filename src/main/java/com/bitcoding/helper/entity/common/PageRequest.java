package com.bitcoding.helper.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * create by: liumeng
 * description: 统一请求参数
 *
 * @author LongQi-Howard
 */
@Data
public class PageRequest implements Serializable {
    public PageRequest(int pageNum, int pageSize, List<FieldQuery> query, List<OrderBy> order) {
        this.pageNum = pageNum == 0 ? 1 : pageNum;
        this.pageSize = pageSize == 0 ? 50 : pageSize;
        this.query = query == null ? new ArrayList<>() : query;
        this.order = order == null ? new ArrayList<>() : order;
    }

    private int pageNum;
    private int pageSize;
    private List<FieldQuery> query;

    private List<OrderBy> order;

    private Map<String, String> export;
}