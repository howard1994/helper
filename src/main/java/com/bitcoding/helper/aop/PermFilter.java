package com.bitcoding.helper.aop;

import java.lang.annotation.*;

/**
 * @author LongQi-Howard
 */

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PermFilter {
    String[] hasRole() default {};
    String[] hasPerm() default {};
}
