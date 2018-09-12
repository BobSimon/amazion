package com.amzics.model.annotation;

import java.lang.annotation.*;

/**
 * 已认证的用户才能访问，目前系统没有权限模块，简单通过该注解拦截一下
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Authen {
}
