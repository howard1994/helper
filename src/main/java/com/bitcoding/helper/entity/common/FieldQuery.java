package com.bitcoding.helper.entity.common;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(name = "字段名", example = "id")
    private String key;
    /**
     * 值
     */
    @ApiModelProperty(name = "字段值", example = "lq123329")
    private Object value;
    /**
     * 操作符 > = < >= <= like
     */
    @ApiModelProperty(name = "操作符", example = "eq")
    private String option;
    /**
     * and || or
     */
    @ApiModelProperty(name = "and条件||or条件", example = "true")
    private Boolean isAnd;
}