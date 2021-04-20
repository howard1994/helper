package com.bitcoding.helper.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * create by: liumeng
 * description: 排序
 *
 * @author LongQi-Howard
 * @return:
 */
@Data
public class OrderBy implements Serializable {
    private String by;

    private String order;

    /**
     * create by: liumeng
     * description: 组装排序字段
     *
     * @param order
     * @return: java.lang.String
     */
    public static String transToOrder(List<OrderBy> order) {
        String queryBy = "";
        if (null == order || order.size() == 0) {
            return queryBy;
        }
        for (OrderBy o : order) {
            if ("".equals(queryBy)) {
                queryBy = o.getBy() + " " + o.getOrder();
            } else {
                queryBy += ", " + o.getBy() + " " + o.getOrder();
            }
        }
        return queryBy;
    }
}