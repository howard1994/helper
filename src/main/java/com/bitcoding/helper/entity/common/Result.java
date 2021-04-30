package com.bitcoding.helper.entity.common;

import com.alibaba.fastjson.annotation.JSONField;
import com.bitcoding.helper.CommonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * create by: liumeng
 * description: TODO
 * create time:  2021/3/25 8:42
 *
 * @author LongQi-Howard
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private Integer code;
    private Boolean success;
    private Long count;
    @JSONField(name = "detail")
    @JsonProperty(value = "detail")
    private String message;
    private T results;

    public Result(Integer code, Boolean success, Long count, String message, T results) {
        this.code = code;
        this.success = success;
        this.count = count;
        this.message = message;
        this.results = results;
    }

    public static Result custom(Integer code, Boolean success, String message, Long count, Object data) {
        return new Result(code, success, count, message, data);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResults() {
        return results;
    }

    public void setResults(T results) {
        this.results = results;
    }
}
