package com.atguigu.gmall0401.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//注解在方法上
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)//运行策略
public @interface LoginRequire {
    //注解参数
    boolean autoRedirect() default true;
}
