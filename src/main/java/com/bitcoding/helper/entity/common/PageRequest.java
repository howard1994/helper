package com.bitcoding.helper.entity.common;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(name = "页数", example ="1")
    private int pageNum;
    @ApiModelProperty(name = "条数", example ="50")
    private int pageSize;
    @ApiModelProperty(name = "筛选字段")
    private List<FieldQuery> query;
    @ApiModelProperty(name = "排序字段")
    private List<OrderBy> order;
    @ApiModelProperty(name = "需要导出字段")
    private Map<String, String> export;
}