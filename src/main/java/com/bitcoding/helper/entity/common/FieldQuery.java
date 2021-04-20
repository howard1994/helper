package com.bitcoding.helper.entity.common;

import lombok.Data;

import java.io.Serializable;

/**
 * create by: liumeng
 * description: 筛选实体类
 *
 * @author LongQi-Howard
 */
@Data
public class FieldQuery implements Serializable {

    public FieldQuery(String key, Object value, String option, Boolean isAnd) {
        this.key = key;
        this.value = value;
        this.isAnd = isAnd;
        switch (option) {
            case "lt":
                this.option = "<";
                break;
            case "gt":
                this.option = ">";
                break;
            case "lte":
                this.option = "<=";
                break;
            case "gte":
                this.option = ">=";
                break;
            case "neq":
                this.option = "<>";
                break;
            case "lk":
                this.option = "like";
                this.value = "%" + value + "%";
                break;
            case "in":
                this.option = "in";
                break;
            case "bt":
                this.option = "between";
                break;
            default:
                this.option = "=";
        }

    }

    private String key;
    /**
     * 值
     */
    private Object value;
    /**
     * 操作符 > = < >= <= like
     */
    private String option;
    /**
     * and || or
     */
    private Boolean isAnd;
}