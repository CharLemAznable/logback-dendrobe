package com.github.charlemaznable.logback.miner.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogbackSql {

    /**
     * 使用的sqlFile路径
     */
    String sqlFile() default "";

    /**
     * 使用的sqlClass, 根据其包路径寻找sqlFile
     */
    Class<?> sqlClass() default Void.class;

    /**
     * 日志插入sql语句的sqlId, 默认为: log{CurrentClassSimpleName}
     */
    String sqlId() default "";
}
