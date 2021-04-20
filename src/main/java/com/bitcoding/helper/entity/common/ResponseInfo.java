package com.bitcoding.helper.entity.common;

import com.bitcoding.helper.CommonUtils;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * create by: liumeng
 * description: TODO
 * create time:  2021/3/24 14:47
 *
 * @author LongQi-Howard
 */
public class ResponseInfo<T> extends ResponseEntity<Result> implements Serializable {


    public ResponseInfo(HttpStatus status) {
        super(status);
    }

    public ResponseInfo(Integer code, Boolean success, Long count, String message, T results) {
        super(Result.custom(code, success, message, count, results), CommonUtils.code2HttpStatus(code));
    }

    public static ResponseInfo success(Integer code) {
        return new ResponseInfo(code, true, null, null, null);
    }

    public static ResponseInfo success(Integer code, String message) {
        return new ResponseInfo(code, true, null, message, null);
    }

    public static ResponseInfo success(Integer code, String message, Long count, Object data) {
        return new ResponseInfo(code, true, count, message, data);
    }

    public static ResponseInfo fail(Integer code) {
        return new ResponseInfo(code, false, null, null, null);
    }

    public static ResponseInfo fail(Integer code, String message) {
        return new ResponseInfo(code, false, null, message, null);
    }

    public static ResponseInfo fail(Integer code, String message, Long count, Object data) {
        return new ResponseInfo(code, false, count, message, data);
    }


}
