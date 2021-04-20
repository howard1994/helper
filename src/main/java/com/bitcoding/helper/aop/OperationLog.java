package com.bitcoding.helper.aop;

import com.bitcoding.helper.entity.common.LogModel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * create by: liumeng
 * description: TODO
 * create time:  2021/3/19 14:29
 *
 * @author LongQi-Howard
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String fromClass() default "Log";

    String level() default "INFO";

    String message() default "调用接口:";
}
