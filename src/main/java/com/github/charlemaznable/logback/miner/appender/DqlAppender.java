package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.FilterReply;
import com.github.charlemaznable.logback.miner.level.Effector;
import lombok.Setter;
import lombok.val;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.github.charlemaznable.logback.miner.appender.DqlCaches.LogbackBeanDqlCache.getLogbackBeanDql;
import static com.github.charlemaznable.logback.miner.appender.DqlCaches.LogbackBeanPresentCache.isLogbackBeanPresent;
import static com.github.charlemaznable.logback.miner.appender.DqlCaches.LogbackPojoSqlCache.getLogbackPojoSql;
import static com.github.charlemaznable.logback.miner.appender.DqlCaches.LogbackSqlCache.useLogbackSql;
import static com.github.charlemaznable.logback.miner.appender.LoggingEventElf.buildEventMap;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class DqlAppender extends AsyncAppender {

    private InternalAppender appender;

    public DqlAppender() {
        this.appender = new InternalAppender();
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.appender.setContext(context);
    }

    public DqlAppender setDqlConnection(String dqlConnection) {
        this.appender.setDqlConnection(dqlConnection);
        return this;
    }

    public DqlAppender setDqlSql(String dqlSql) {
        this.appender.setDqlSql(dqlSql);
        return this;
    }

    @Override
    protected UnsynchronizedAppenderBase<ILoggingEvent> internalAppend() {
        return this.appender;
    }

    @Override
    protected FilterReply decide(Effector effector, Level eventLevel) {
        // configured DqlAppender and event passed EffectorTurboFilter,
        // but appender level is greater then event level -> DENY
        if (effector.getDqlEffectiveLevelInt() > eventLevel.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

    static class InternalAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

        @Setter
        private String dqlConnection;
        @Setter
        private String dqlSql;

        @Override
        protected void append(ILoggingEvent eventObject) {
            if (!isStarted()) return;

            try {
                DqlExecuteWrapper.preExecute(eventObject);
                // 公共参数, 包含event/mdc/ctx-property
                val paramMap = buildEventMap(eventObject);

                val argumentArray = defaultIfNull(eventObject.getArgumentArray(), new Object[0]);
                val arguments = Arrays.stream(argumentArray)
                        .filter(arg -> nonNull(arg) && isLogbackBeanPresent(arg.getClass()))
                        .collect(Collectors.toList());
                // 日志不包含@LogbackBean注解的参数, 执行默认连接的默认SQL
                if (arguments.isEmpty()) {
                    val dql = getLogbackBeanDql(null, dqlConnection);
                    // 未指定默认连接或默认SQL, 则跳过
                    if (isNull(dql) || isBlank(dqlSql)) return;
                    dql.params(paramMap).dynamics(paramMap).execute(dqlSql);
                    return;
                }

                // 遍历@LogbackBean注解的参数
                for (val argument : arguments) {
                    val clazz = argument.getClass();
                    val dql = getLogbackBeanDql(clazz, dqlConnection);
                    // 参数类型注解未指定连接, 且Logger未指定默认连接, 则跳过
                    if (isNull(dql)) continue;

                    // 设参数key为arg, 加入eql参数上下文
                    val currentMap = newHashMap(paramMap);
                    currentMap.put("arg", argument);
                    // 同时设置一般参数与动态参数
                    dql.params(currentMap).dynamics(currentMap);

                    // 指定sqlFile的情形
                    if (useLogbackSql(clazz, dql)) dql.execute();
                        // 根据POJO生成直接SQL
                    else dql.execute(getLogbackPojoSql(clazz));
                }

            } finally {
                DqlExecuteWrapper.afterExecute(eventObject);
            }
        }
    }
}
