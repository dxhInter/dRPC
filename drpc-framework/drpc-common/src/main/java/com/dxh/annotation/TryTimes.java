package com.dxh.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DRPC服务注解,在类上使用，在运行时有效，用于提供服务重试次数
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TryTimes {
    int tryTimes() default 1;
    int intervalTime() default 2000;

}
