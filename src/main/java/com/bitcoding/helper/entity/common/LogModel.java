package com.bitcoding.helper.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * create by: liumeng
 * description: 日志模板
 * create time: 2020/11/18 17:03
 *
 * @author LongQi-Howard
 */
@Data
public class LogModel implements Serializable {
    private String fromApp;
    private String fromClass;
    private String level;
    private String message;
    private Date createTime;
    private Object param;

    public LogModel(String fromClass, String level, String message, Object param) {
        this.fromApp = "spring boot";
        this.fromClass = fromClass;
        this.level = level;
        this.message = message;
        this.createTime = new Date();
        this.param = param;
    }

}
