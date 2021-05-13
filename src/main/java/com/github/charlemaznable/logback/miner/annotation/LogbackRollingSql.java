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
public @interface LogbackRollingSql {

    /**
     * 表名滚动模式, 仅支持时间日期模式
     */
    String tableNamePattern();

    /**
     * 使用的sqlFile路径
     */
    String sqlFile() default "";

    /**
     * 使用的sqlClass, 根据其包路径寻找sqlFile
     */
    Class<?> sqlClass() default Void.class;

    /**
     * 滚动日志表准备sql语句的sqlId, 默认为: prepare{CurrentClassSimpleName}
     */
    String sqlId() default "";
}
